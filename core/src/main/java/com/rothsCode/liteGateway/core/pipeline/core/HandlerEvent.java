package com.rothsCode.liteGateway.core.pipeline.core;

import com.rothsCode.liteGateway.core.Context.GatewayContext;
import com.rothsCode.liteGateway.core.Context.GatewayContextStatusEnum;
import com.rothsCode.liteGateway.core.exception.GatewayException;
import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.model.Result;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

/**
 * @author rothscode
 * @Description:请求处理事件基类
 * @date 2022/7/30 17:38
 */
@Slf4j
public abstract class HandlerEvent implements IHandlerEvent<HandlerContext> {

  public HandlerEvent next;

  public void addNext(HandlerEvent next) {
    this.next = next;
  }

  public HandlerEvent getNext() {
    return next;
  }

  /**
   * 获取后置处理器 默认PROXY_ROUTE_EVENT之后的事件都为后置事件
   *
   * @return
   */
  public HandlerEvent getPostProcess() {
    while (next != null && !next.handleEvent().equals(HandleEventEnum.PROXY_ROUTE_EVENT)) {
      if (null != next) {
        next = next.next;
      }
    }
    if (next == null) {
      return null;
    }
    return next.next;
  }

  /**
   * 事件链当前节点如果报错则抛出异常
   *
   * @param t
   */
  @Override
  public void doHandle(HandlerContext t) {
    boolean continueFlag = false;
    try {
      continueFlag = this.actualProcess(t);
    } catch (Throwable e) {
      String message = GatewayRequestStatusEnum.INTERNAL_ERROR.getDesc();
      if (StringUtils.isNotBlank(e.getMessage())) {
        if (e.getMessage().length() > 100) {
          message = e.getMessage().substring(0, 100);
        } else {
          message = e.getMessage();
        }
      }
      ByteBuf byteBuf = Unpooled
          .wrappedBuffer(Result.errorGatewayError(message).toString().getBytes());
      FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
          HttpResponseStatus.valueOf(HttpStatus.SC_BAD_GATEWAY),
          byteBuf);
      GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
      gatewayContext.getGatewayRequest().writeResponse(httpResponse);
      //释放资源
      gatewayContext.releaseRequest();
      gatewayContext.setThrowable(e);
      gatewayContext.setStatus(GatewayContextStatusEnum.WRITE_FLUSH);
      gatewayContext.setGatewayStatus(GatewayRequestStatusEnum.INTERNAL_ERROR);
      //发生异常返回响应信息后,跳转后置处理器执行后续逻辑,再发生异常则只记录不做业务处理
      try {
        HandlerEvent postEvent = getPostProcess();
        if (postEvent != null) {
          postEvent.doHandle(t);
        }
      } catch (Throwable postThrowable) {
        log.error("postChain error:{}", postThrowable);
      }
    }
    if (continueFlag && next != null) {
      next.doHandle(t);
    }

  }

  /**
   * 事件链实际实现的处理方法 boolean 是否需要继续执行后续链路
   *
   * @param t
   */
  public abstract boolean actualProcess(HandlerContext t) throws GatewayException;

}