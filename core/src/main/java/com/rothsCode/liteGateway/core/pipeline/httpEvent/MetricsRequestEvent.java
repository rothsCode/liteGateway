package com.rothsCode.liteGateway.core.pipeline.httpEvent;

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.plugin.metrics.memoryMetrics.StatisticsRollingNumber;
import com.rothsCode.liteGateway.core.plugin.metrics.remoteMetrics.MetricsReporter;
import com.rothsCode.liteGateway.core.plugin.metrics.remoteMetrics.StatisticsTypeEnum;
import com.rothsCode.liteGateway.core.util.ThreadFactoryImpl;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: 网关整体指标情况监控
 * @date 2023/8/13 21:42
 */

public class MetricsRequestEvent extends HandlerEvent {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsRequestEvent.class);

  private StatisticsRollingNumber globalRollingNumber;

  private ServerConfig serverConfig;

  public MetricsRequestEvent() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    if (!serverConfig.getMetricsEnabled()) {
      //周期一分钟，6个槽的环形数组
      globalRollingNumber = new StatisticsRollingNumber("global", 12 * 1000, 12);
      ScheduledExecutorService globalMemoryIndexExecutor =
          Executors
              .newSingleThreadScheduledExecutor(new ThreadFactoryImpl("globalMemoryIndexThread"));
      globalMemoryIndexExecutor.scheduleAtFixedRate(() -> {
        //打印指标内存统计数据
        printGlobalStatisticsIndex(globalRollingNumber);
      }, 10, 12, TimeUnit.SECONDS);
    }
  }


  @Override
  public boolean actualProcess(HandlerContext t) {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    gatewayContext.setEndTime(System.currentTimeMillis());
    if (serverConfig.getMetricsEnabled()) {
      metricsRequestReporter(gatewayContext);
    } else {
      statisticsGatewayRequest(gatewayContext, globalRollingNumber);
    }
    return true;
  }

  /**
   * prometheus数据上报
   *
   * @param gatewayContext
   */
  private void metricsRequestReporter(GatewayContext gatewayContext) {
    //整体维度
    MetricsReporter
        .counterIncrement(StatisticsTypeEnum.TOTAL.getCode());
    if (gatewayContext.getThrowable() != null) {
      MetricsReporter
          .counterIncrement(StatisticsTypeEnum.ERROR.getCode());
    } else {
      MetricsReporter
          .counterIncrement(StatisticsTypeEnum.SUCCESS.getCode());
    }
    //接口耗时
    long apiCostTime = gatewayContext.getRouteReturnTime() - gatewayContext.getRouteStartTime();
    MetricsReporter
        .recordTime(StatisticsTypeEnum.API_TIME.getCode(), apiCostTime);
    //网关前置处理时间
    MetricsReporter
        .recordTime(StatisticsTypeEnum.PRE_GATEWAY_TIME.getCode(),
            gatewayContext.getRouteStartTime() - gatewayContext.getStartTime());
    //网关后置处理时间
    MetricsReporter
        .recordTime(StatisticsTypeEnum.POST_GATEWAY_TIME.getCode(),
            System.currentTimeMillis() - gatewayContext.getRouteReturnTime());
  }

  /**
   * 内存数据统计
   *
   * @param gatewayContext
   * @param statisticsRollingNumber
   */
  private void statisticsGatewayRequest(GatewayContext gatewayContext,
      StatisticsRollingNumber statisticsRollingNumber) {
    statisticsRollingNumber.increment(StatisticsTypeEnum.TOTAL);
    if (gatewayContext.getThrowable() != null) {
      statisticsRollingNumber.increment(StatisticsTypeEnum.ERROR);
    } else {
      statisticsRollingNumber.increment(StatisticsTypeEnum.SUCCESS);
    }
    //接口耗时
    long apiCostTime = gatewayContext.getRouteReturnTime() - gatewayContext.getRouteStartTime();
    statisticsRollingNumber.add(StatisticsTypeEnum.API_TOTAL_TIME, apiCostTime);
    //网关处理耗时
    long gatewayCostTime =
        gatewayContext.getEndTime() - gatewayContext.getStartTime() - apiCostTime;
    statisticsRollingNumber.add(StatisticsTypeEnum.GATEWAY_TOTAL_TIME, gatewayCostTime);
    //接口调用时间峰值
    statisticsRollingNumber.updateRollingMax(StatisticsTypeEnum.MAX_TIME, apiCostTime);
  }

  /**
   * 打印内存统计指标数据 TODO 后续优化
   *
   * @param globalRollingNumber
   */
  private void printGlobalStatisticsIndex(StatisticsRollingNumber globalRollingNumber) {
    if (globalRollingNumber.getCumulativeSum(StatisticsTypeEnum.TOTAL) > 0) {
      long[] apiInvokeTotal = globalRollingNumber.getValues(StatisticsTypeEnum.TOTAL);
      long[] apiTotalTime = globalRollingNumber.getValues(StatisticsTypeEnum.API_TOTAL_TIME);
      long[] apiAvgTime = new long[apiInvokeTotal.length];
      for (int i = 0; i < apiInvokeTotal.length; i++) {
        if (apiInvokeTotal[i] > 0) {
          apiAvgTime[i] = apiTotalTime[i] / apiInvokeTotal[i];
        }
      }
      long[] gatewayTotalTime = globalRollingNumber
          .getValues(StatisticsTypeEnum.GATEWAY_TOTAL_TIME);
      long[] gatewayAvgTime = new long[gatewayTotalTime.length];
      for (int i = 0; i < gatewayTotalTime.length; i++) {
        if (gatewayTotalTime[i] > 0) {
          gatewayAvgTime[i] = gatewayTotalTime[i] / apiInvokeTotal[i];
        }
      }
      LOGGER.info("index—apiInvokeTotal:{}", apiInvokeTotal);
      LOGGER
          .info("index—apiInvokeError:{}", globalRollingNumber.getValues(StatisticsTypeEnum.ERROR));
      LOGGER.info("index—apiInvokeCostTime:{}", apiAvgTime);
      LOGGER.info("index—apiGatewayCostTime:{}", gatewayAvgTime);
    }
  }

  @Override
  public HandleEventEnum handleEvent() {
    return HandleEventEnum.METRICS_REQUEST;
  }
}
