package com.rothsCode.liteGateway.core.plugin.log.logReporter;

import com.rothsCode.liteGateway.core.plugin.log.logCollector.GatewayRequestLog;

/**
 * @author roths
 * @Description: 日志上报
 * @date 2023/8/29 20:17
 */
public interface LogReporter {

  /**
   * 日志上报
   *
   * @param gatewayRequestLog
   */
  void reportLog(GatewayRequestLog gatewayRequestLog);
}
