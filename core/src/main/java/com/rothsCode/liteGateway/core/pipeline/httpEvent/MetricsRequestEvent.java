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

/**
 * @author roths
 * @Description: 网关整体指标情况监控
 * @date 2023/8/13 21:42
 */

public class MetricsRequestEvent extends HandlerEvent {

  private StatisticsRollingNumber globalRollingNumber;
  private ServerConfig serverConfig;

  public MetricsRequestEvent() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    if (!serverConfig.getMetricsEnabled()) {
      globalRollingNumber = new StatisticsRollingNumber("global", 60 * 1000, 60);
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
    //统计埋点
    MetricsReporter
        .recordTime(StatisticsTypeEnum.GATEWAY_TIME.getCode(),
            gatewayContext.getRouteStartTime() - gatewayContext.getStartTime());
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
    //调用峰值
    statisticsRollingNumber.updateRollingMax(StatisticsTypeEnum.MAX_TIME, apiCostTime);
  }

  @Override
  public HandleEventEnum handleEvent() {
    return HandleEventEnum.METRICS_REQUEST;
  }
}
