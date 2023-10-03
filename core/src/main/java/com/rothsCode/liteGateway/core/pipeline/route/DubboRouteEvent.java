package com.rothsCode.liteGateway.core.pipeline.route;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.container.Context.RequestWriteStatusEnum;
import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.model.DubboServiceInvoker;
import com.rothsCode.liteGateway.core.model.Result;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.RouteTypeEnum;
import com.rothsCode.liteGateway.core.util.DubboInvokeHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description:dubbo路由事件 处理请求头传递
 * @date 2023/8/17 19:12
 */
public class DubboRouteEvent extends HandlerEvent {

  public static final Logger log = LoggerFactory.getLogger(DubboRouteEvent.class);

  @Override
  public boolean actualProcess(HandlerContext t) {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    FullHttpRequest fullHttpRequest = (FullHttpRequest) gatewayContext.getGatewayRequest().getMsg();
    if (!RouteTypeEnum.DUBBO.equals(gatewayContext.getRouteType())) {
      return true;
    }
    if (RequestWriteStatusEnum.WRITE.equals(gatewayContext.getWriteStatus())) {
      log.error("request repeat write skip:{}", JSONObject.toJSONString(gatewayContext));
      return true;
    }
    DubboServiceInvoker dubboServiceInvoker = gatewayContext.getDubboServiceInvoker();
    Assert.notNull(dubboServiceInvoker, "dubbo服务调用实例不存在");
    //dubbo参数透传
    Map<String, String> headerMap = new HashMap<>();
    for (Iterator<Entry<String, String>> it = fullHttpRequest.headers().iteratorAsString(); it
        .hasNext(); ) {
      Entry<String, String> entry = it.next();
      headerMap.put(entry.getKey(), entry.getValue());
    }
    RpcContext.getContext().getAttachments().putAll(headerMap);
    //获取服务
    GenericService genericService = DubboInvokeHelper
        .getGenericService(GatewayConfigLoader.getInstance().getServerConfig(),
            dubboServiceInvoker);
    CompletableFuture<Object> future = genericService
        .$invokeAsync(dubboServiceInvoker.getMethodName(), dubboServiceInvoker.getParamTypes(),
            dubboServiceInvoker.getParamValues());
    //获取结果
    future.whenComplete((value, throwable) -> {
      handleDubboResponse(value, throwable, gatewayContext, t);
    });
    return false;
  }

  private void handleDubboResponse(Object gatewayResponse, Throwable throwable,
      GatewayContext gatewayContext, HandlerContext t) {
    FullHttpResponse httpResponse = null;
    String message = GatewayRequestStatusEnum.DUBBO_ERROR.getDesc();
    try {
      //释放资源
      gatewayContext.releaseRequest();
      //设置返回时间
      gatewayContext.setRouteReturnTime(System.currentTimeMillis());
      if (gatewayResponse != null) {
        gatewayContext.setResponse(gatewayResponse);
      }
      ByteBuf byteBuf = null;
      if (throwable != null) {
        gatewayContext.setThrowable(throwable);
        if (StringUtils.isNotBlank(throwable.getMessage())) {
          if (throwable.getMessage().length() > 100) {
            message = throwable.getMessage().substring(0, 100);
          } else {
            message = throwable.getMessage();
          }
        }
        byteBuf = Unpooled.wrappedBuffer(Result.errorGatewayError(message).toString().getBytes());
        //构建相应体
        httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.BAD_GATEWAY,
            byteBuf);
      } else {
        if (gatewayResponse != null) {
          //map转json
          if (gatewayResponse instanceof Map) {
            Map<String, Object> respMap = (Map<String, Object>) gatewayResponse;
            respMap.remove("class");
            byteBuf = Unpooled.wrappedBuffer(JSONObject.toJSONString(respMap).getBytes());
          } else {
            byteBuf = Unpooled.wrappedBuffer(JSONObject.toJSONString(gatewayResponse).getBytes());
          }
        } else {
          byteBuf = Unpooled.wrappedBuffer("".getBytes());
        }
        httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            byteBuf);
      }
    } catch (Exception e) {
      log.error("handleDubboResponseError:{}", e);
      if (gatewayContext.getThrowable() == null) {
        gatewayContext.setThrowable(e);
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
    return HandleEventEnum.DUBBO_ROUTE_EVENT;
  }
}
