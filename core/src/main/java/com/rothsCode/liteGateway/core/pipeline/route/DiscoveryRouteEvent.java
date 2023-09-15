package com.rothsCode.liteGateway.core.pipeline.route;

import cn.hutool.core.net.url.UrlBuilder;
import com.alibaba.fastjson.JSONObject;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.constants.GatewayConstant;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.container.Context.RequestWriteStatusEnum;
import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.model.Result;
import com.rothsCode.liteGateway.core.model.ServiceInfo;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.ProtocolTypeEnum;
import com.rothsCode.liteGateway.core.plugin.core.PluginManager;
import com.rothsCode.liteGateway.core.plugin.httpClient.IAsyncHttpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description:服务路由事件
 * @date 2023/8/16 18:01
 */

public class DiscoveryRouteEvent extends HandlerEvent {

  public static final Logger log = LoggerFactory.getLogger(DiscoveryRouteEvent.class);
  private ServerConfig serverConfig;

  private IAsyncHttpClient asyncHttpClient;

  public DiscoveryRouteEvent() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    asyncHttpClient = (IAsyncHttpClient) PluginManager.getInstance()
        .getPluginByName(serverConfig.getHttpClientType());
  }

  @Override
  public boolean actualProcess(HandlerContext t) {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    //跳过调用
    if (!ProtocolTypeEnum.DISCOVERY.equals(gatewayContext.getProtocol())) {
      return true;
    }
    if (RequestWriteStatusEnum.WRITE.equals(gatewayContext.getWriteStatus())) {
      log.error("请求重复写入跳过:{}", JSONObject.toJSONString(gatewayContext));
      return true;
    }
    gatewayContext.setRouteStartTime(System.currentTimeMillis());
    FullHttpRequest fullHttpRequest = (FullHttpRequest) gatewayContext.getGatewayRequest().getMsg();
    ServiceInfo serviceInfo = gatewayContext.getServiceInfo();
    String targetUrl = UrlBuilder.create().setScheme(GatewayConstant.HTTP_SCHEME)
        .setHost(serviceInfo.getIp())
        .setPort(serviceInfo.getPort())
        .addPath(fullHttpRequest.uri())
        .build();
    RequestBuilder requestBuilder = new RequestBuilder();
    requestBuilder.setUrl(targetUrl);
    requestBuilder.setMethod(fullHttpRequest.method().name());
    requestBuilder.setHeaders(fullHttpRequest.headers());
    requestBuilder.setBody(fullHttpRequest.content().nioBuffer());
    //异步调用
    CompletableFuture<Response> future = asyncHttpClient.executeRequest(requestBuilder.build());
    //异步处理返回结果
    future.whenCompleteAsync((response, throwable) -> {
      handleHttpResponse(response, throwable, gatewayContext, t);
    });
    return false;
  }

  private void handleHttpResponse(Response response, Throwable throwable,
      GatewayContext gatewayContext, HandlerContext t) {
    FullHttpResponse httpResponse = null;
    String message = GatewayRequestStatusEnum.SERVICE_ERROR.getDesc();
    try {
      //释放资源
      gatewayContext.releaseRequest();
      //设置返回时间
      gatewayContext.setRouteReturnTime(System.currentTimeMillis());
      gatewayContext.setResponse(response);
      ByteBuf byteBuf = null;
      if (throwable != null) {
        gatewayContext.setThrowable(throwable);
        message = subMessage(throwable, message);
        byteBuf = Unpooled.wrappedBuffer(Result.errorGatewayError(message).toString().getBytes());
        //构建相应体
        httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.BAD_GATEWAY,
            byteBuf);
      } else {
        byteBuf = Unpooled.wrappedBuffer(response.getResponseBodyAsByteBuffer());
        httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            byteBuf);
      }
    } catch (Exception e) {
      log.error("handleHttpResponseError:{}", e);
      //优先记录接口返回错误
      if (gatewayContext.getThrowable() == null) {
        gatewayContext.setThrowable(e);
        //兜底处理
        message = subMessage(e, message);
        httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.BAD_GATEWAY,
            Unpooled.wrappedBuffer(Result.errorGatewayError(message).toString().getBytes()));
      }
    } finally {
      gatewayContext.writeResponse(httpResponse);
      //异步线程手动触发下一个事件
      getNext().doHandle(t);
    }
  }

  private String subMessage(Throwable throwable, String message) {
    if (StringUtils.isNotBlank(throwable.getMessage())) {
      if (throwable.getMessage().length() > 100) {
        message = throwable.getMessage().substring(0, 100);
      } else {
        message = throwable.getMessage();
      }
    }
    return message;
  }

  @Override
  public HandleEventEnum handleEvent() {
    return HandleEventEnum.DISCOVERY_ROUTE_EVENT;
  }
}
