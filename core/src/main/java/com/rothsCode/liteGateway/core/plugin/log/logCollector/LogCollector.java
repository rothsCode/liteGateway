package com.rothsCode.liteGateway.core.plugin.log.logCollector;

import java.util.List;

/**
 * @author roths
 * @Description: 日志上报
 * @date 2023/8/29 20:17
 */
public interface LogCollector {

  void collectLog(GatewayRequestLog gatewayRequestLog);

  void batchCollectLog(List<GatewayRequestLog> requestLogs);
}
