package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl;

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import com.rothsCode.liteGateway.core.plugin.log.logCollector.GatewayRequestLog;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.LogReporter;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsClientServiceClient;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.entity.GatewayLogEsEntity;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: es收集器
 * @date 2023/8/29 21:15
 */
public class EsLogCollectorClient implements Plugin, LogReporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(EsLogCollectorClient.class);

  private ServerConfig serverConfig;

  private EsClientServiceClient esClientServiceClient;

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
    esClientServiceClient = EsClientServiceClient.getInstance();
    esClientServiceClient.init();
    LOGGER.info("EsLogCollectorClient has been initialized");
  }

  @Override
  public void start() {

  }

  @Override
  public void shutDown() {
    esClientServiceClient.shutDown();
  }

  @Override
  public void reportLog(GatewayRequestLog gatewayRequestLog) {
    GatewayLogEsEntity gatewayLogEsEntity = GatewayLogEsEntity.builder()
        .gatewayStatus(gatewayRequestLog.getGatewayStatus())
        .clientIp(gatewayRequestLog.getClientIp())
        .host(gatewayRequestLog.getHost())
        .invokeStatus(gatewayRequestLog.getInvokeStatus())
        .method(gatewayRequestLog.getMethod())
        .module(gatewayRequestLog.getModule())
        .queryParams(gatewayRequestLog.getQueryParams())
        .requestBody(gatewayRequestLog.getRequestBody())
        .requestHeader(gatewayRequestLog.getRequestHeader())
        .requestTime(gatewayRequestLog.getRequestTime())
        .requestUri(gatewayRequestLog.getRequestUri())
        .responseBody(gatewayRequestLog.getResponseBody())
        .responseHeader(gatewayRequestLog.getResponseHeader())
        .responseStatus(gatewayRequestLog.getResponseStatus())
        .rpcType(gatewayRequestLog.getRpcType())
        .traceId(gatewayRequestLog.getTraceId())
        .throwable(gatewayRequestLog.getThrowable())
        .build();
    try {
      IndexResponse indexResponse = esClientServiceClient.index(gatewayLogEsEntity);
      if (indexResponse == null || !Result.CREATED.equals(indexResponse.getResult())) {
        LOGGER.error("syncGatewayLogEs created error:{}", indexResponse);
      }
    } catch (Exception e) {
      LOGGER.error("syncGatewayLogEs error:{}", e);
    }
  }
}
