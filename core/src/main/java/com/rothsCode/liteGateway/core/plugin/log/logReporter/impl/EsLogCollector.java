package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl;

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import com.rothsCode.liteGateway.core.plugin.log.logCollector.GatewayRequestLog;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.LogReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: es收集器
 * @date 2023/8/29 21:15
 */
public class EsLogCollector implements Plugin, LogReporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(EsLogCollector.class);
  private ServerConfig serverConfig;

  @Override
  public boolean checkInit() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    return PluginEnum.ES.getCode().equals(serverConfig.getLogClientType());
  }

  @Override
  public String pluginName() {
    return PluginEnum.ES.getCode();
  }

  @Override
  public void init() {
    LOGGER.info("EsLogCollector has been initialized");
  }

  @Override
  public void start() {

  }

  @Override
  public void shutDown() {

  }

  @Override
  public void reportLog(GatewayRequestLog gatewayRequestLog) {

  }
}
