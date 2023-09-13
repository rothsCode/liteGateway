package com.rothsCode.liteGateway.core.pipeline.httpEvent;

import com.rothsCode.liteGateway.core.Context.GatewayContext;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.plugin.metrics.memoryMetrics.StatisticsRollingNumber;
import com.rothsCode.liteGateway.core.plugin.metrics.remoteMetrics.MetricsReporter;
import com.rothsCode.liteGateway.core.plugin.metrics.remoteMetrics.StatisticsTypeEnum;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author roths
 * @Description: 网关整体指标情况监控
 * @date 2023/8/13 21:42
 */

public class MetricsRequestEvent extends HandlerEvent {

  private static final String GLOBAL_ROLLING_NUMBER = "global";
  private Map<String, StatisticsRollingNumber> rollingNumberMap = null;
  private ServerConfig serverConfig;

  public MetricsRequestEvent() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    if (!serverConfig.getMetricsEnabled()) {
      rollingNumberMap = new ConcurrentHashMap<>(16);
      StatisticsRollingNumber statisticsRollingNumber = new StatisticsRollingNumber(
          GLOBAL_ROLLING_NUMBER, 60 * 1000, 60);
      rollingNumberMap.put(GLOBAL_ROLLING_NUMBER, statisticsRollingNumber);
    }
  }

  @Override
  public boolean actualProcess(HandlerContext t) {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    gatewayContext.setEndTime(System.currentTimeMillis());
    if (serverConfig.getMetricsEnabled()) {
      metricsRequestReporter(gatewayContext);
    } else {
      statisticsRollingNumber(gatewayContext);
    }
    return true;
  }

  private void statisticsRollingNumber(GatewayContext gatewayContext) {
    StatisticsRollingNumber statisticsRollingNumber = rollingNumberMap.get(GLOBAL_ROLLING_NUMBER);
    //提取统计值放入环形数组
    statisticsGatewayRequest(gatewayContext, statisticsRollingNumber);
    StatisticsRollingNumber apiRollingNumber = rollingNumberMap.get(gatewayContext.getUrlPath());
    if (apiRollingNumber == null) {
      //考虑性能窗口区间取一分钟,10个时间窗口
      apiRollingNumber = new StatisticsRollingNumber(gatewayContext.getUrlPath(), 60 * 1000 * 10,
          10);
      rollingNumberMap.put(gatewayContext.getUrlPath(), apiRollingNumber);
    }
    //接口维度
    statisticsGatewayRequest(gatewayContext, apiRollingNumber);
  }

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
