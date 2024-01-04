package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl;

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import com.rothsCode.liteGateway.core.plugin.log.logCollector.GatewayRequestLog;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.LogReporter;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.hbase.HbaseClientServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: es收集器
 * @date 2023/8/29 21:15
 */
public class HbaseLogCollectorClient implements Plugin, LogReporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(HbaseLogCollectorClient.class);

  private ServerConfig serverConfig;

  private HbaseClientServiceClient hbaseClientServiceClient;

  @Override
  public boolean checkInit() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    return PluginEnum.HBASE.getCode().equals(serverConfig.getLogClientType());
  }

  @Override
  public String pluginName() {
    return PluginEnum.HBASE.getCode();
  }

  @Override
  public void init() {
    hbaseClientServiceClient = HbaseClientServiceClient.getInstance();
    hbaseClientServiceClient.init();
    LOGGER.info("HbaseLogCollectorClient has been initialized");
  }

  @Override
  public void start() {

  }

  @Override
  public void shutDown() {
    hbaseClientServiceClient.shutDown();
  }

  @Override
  public void reportLog(GatewayRequestLog gatewayRequestLog) {
    try {
      hbaseClientServiceClient.saveGatewayLog(gatewayRequestLog);
    } catch (Exception e) {
      LOGGER.error("syncGatewayLogEs error:{}", e);
    }
  }
}
