package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl;

import com.alibaba.fastjson.JSONObject;
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
 * @Description: 日志文件客户端
 * @date 2023/9/5 17:42
 */
public class FileLogClient implements Plugin, LogReporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileLogClient.class);
  private ServerConfig serverConfig;

  private FileAppender fileAppender;

  @Override
  public boolean checkInit() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    return PluginEnum.FILE_STORAGE.getCode().equals(serverConfig.getLogClientType());
  }

  @Override
  public String pluginName() {
    return PluginEnum.FILE_STORAGE.getCode();
  }

  @Override
  public void init() {
    fileAppender = new FileAppender(serverConfig.getFileStorePath(), serverConfig.getLogFileName());
    LOGGER.info("FileLogClient has been initialized");
  }

  @Override
  public void start() {

  }

  @Override
  public void shutDown() {
    fileAppender.close();
  }

  @Override
  public void reportLog(GatewayRequestLog gatewayRequestLog) {
    fileAppender.append(JSONObject.toJSONString(gatewayRequestLog).getBytes());
  }
}
