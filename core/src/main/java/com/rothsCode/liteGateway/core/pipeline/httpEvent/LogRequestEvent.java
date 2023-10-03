package com.rothsCode.liteGateway.core.pipeline.httpEvent;

import static java.nio.charset.StandardCharsets.UTF_8;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import com.rothsCode.liteGateway.core.plugin.core.PluginManager;
import com.rothsCode.liteGateway.core.plugin.log.logCollector.DefaultLogCollector;
import com.rothsCode.liteGateway.core.plugin.log.logCollector.GatewayRequestLog;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.Date;
import org.asynchttpclient.netty.NettyResponse;

/**
 * @author roths
 * @Description: 日志采集上报事件
 * @date 2023/8/13 21:42
 */

public class LogRequestEvent extends HandlerEvent {


  private DefaultLogCollector logCollector;

  public LogRequestEvent() {
    logCollector = (DefaultLogCollector) PluginManager.getInstance()
        .getPluginByName(PluginEnum.LOG_COLLECT.getCode());
  }

  @Override
  public boolean actualProcess(HandlerContext t) {
    if (logCollector != null) {
      GatewayRequestLog gatewayRequestLog = buildGatewayLog(t);
      if (gatewayRequestLog != null) {
        logCollector.collectLog(gatewayRequestLog);
      }
    }
    return true;
  }

  private GatewayRequestLog buildGatewayLog(HandlerContext t) {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    gatewayContext.setEndTime(System.currentTimeMillis());
    FullHttpRequest fullHttpRequest = (FullHttpRequest) gatewayContext.getGatewayRequest().getMsg();
    GatewayRequestLog gatewayRequestLog = GatewayRequestLog.builder()
        .requestBody(fullHttpRequest.content().toString(UTF_8))
        .requestHeader(fullHttpRequest.headers().toString())
        .requestUri(fullHttpRequest.uri())
        .clientIp(gatewayContext.getClientIP())
        .method(fullHttpRequest.method().name())
        .path(gatewayContext.getUrlPath())
        .queryParams(new QueryStringDecoder(fullHttpRequest.uri()).rawQuery())
        .rpcType(gatewayContext.getRouteType().getCode())
        .gatewayStatus(gatewayContext.getWriteStatus().getCode())
        .traceId(gatewayContext.getTraceId())
        .module(gatewayContext.getServiceName())
        .requestTime(DateUtil.formatDateTime(new Date()))
        .throwable(gatewayContext.getThrowable())
        .build();
    if (gatewayContext.getResponse() != null) {
      if (gatewayContext.getResponse() instanceof NettyResponse) {
        NettyResponse response = (NettyResponse) gatewayContext.getResponse();
        gatewayRequestLog.setResponseBody(response.getResponseBody());
        if (response.getHeaders() != null) {
          gatewayRequestLog.setResponseHeader(JSONObject.toJSONString(response.getHeaders()));
        }
        gatewayRequestLog.setInvokeStatus(response.getStatusCode());
      } else {
        gatewayRequestLog.setResponseBody(gatewayContext.getResponse().toString());
      }
    }
    return gatewayRequestLog;
  }

  @Override
  public HandleEventEnum handleEvent() {
    return HandleEventEnum.LOG_REQUEST_EVENT;
  }
}
