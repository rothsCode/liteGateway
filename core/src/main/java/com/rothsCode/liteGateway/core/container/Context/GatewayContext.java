package com.rothsCode.liteGateway.core.container.Context;

import com.alibaba.fastjson.JSONObject;
import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.model.DubboServiceInvoker;
import com.rothsCode.liteGateway.core.model.ProxyRouteRule;
import com.rothsCode.liteGateway.core.model.ServiceInfo;
import com.rothsCode.liteGateway.core.pipeline.enums.ProtocolTypeEnum;
import com.rothsCode.liteGateway.core.plugin.metrics.memoryMetrics.StatisticsRollingNumber;
import com.rothsCode.liteGateway.core.plugin.metrics.remoteMetrics.StatisticsTypeEnum;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: 网关请求全上下文
 * @date 2023/8/14 18:28
 */
@Data
public class GatewayContext {


  private static final Logger LOGGER = LoggerFactory.getLogger(GatewayContext.class);

  /**
   * 网关链路id
   */

  private String traceId;

  /**
   * 请求体
   */
  private GatewayRequest gatewayRequest;

  /**
   * 接口相应信息
   */
  private Object response;

  /**
   * http服务调用信息
   */
  private ServiceInfo serviceInfo;

  /**
   * 代理调用信息
   */
  private ProxyRouteRule proxyRouteRule;
  /**
   * 调用路径
   */
  private String urlPath;

  private String clientIP;


  /**
   * 调用服务名
   */
  private String serviceName;

  /**
   * dubbo服务调用信息
   */
  private DubboServiceInvoker dubboServiceInvoker;

  /**
   * 调用协议
   */
  private ProtocolTypeEnum protocol = ProtocolTypeEnum.DISCOVERY;

  /**
   * 负载均衡策略
   */
  private String loadBalanceStrategy;

  /**
   * 网关异常
   */
  private Throwable throwable;

  /**
   * 数据写入状态
   */
  private RequestWriteStatusEnum writeStatus = RequestWriteStatusEnum.ENTER;

  /**
   * 网关请求状态
   */
  private GatewayRequestStatusEnum gatewayStatus = GatewayRequestStatusEnum.SUCCESS;

  /**
   * 业务请求返回状态
   */
  private int responseStatus;
  /**
   * 请求开始时间
   */
  private long startTime;

  /**
   * 请求转发开始时间
   */
  private long routeStartTime;

  /**
   * 请求路由返回时间
   */
  private long routeReturnTime;

  /**
   * 请求网关结束时间
   */
  private long endTime;

  private AtomicBoolean requestReleased = new AtomicBoolean(false);

  /**
   * 释放fullHttpRequest
   */
  public void releaseRequest() {
    if (requestReleased.compareAndSet(false, true)) {
      ReferenceCountUtil.release(gatewayRequest.getMsg());
    }
  }

  /**
   * 读写QPS统计
   */
  private StatisticsRollingNumber channelQPSRollingNumber;

  /**
   * 回写响应
   *
   * @param httpResponse
   */
  public void writeResponse(FullHttpResponse httpResponse) {
    if (RequestWriteStatusEnum.WRITE.equals(writeStatus)) {
      LOGGER.error("响应已写入:{}", JSONObject.toJSONString(httpResponse));
      return;
    }
    //水位控制
    writeFlow(gatewayRequest.getCtx());
    gatewayRequest.getCtx().writeAndFlush(httpResponse)
        .addListener((ChannelFutureListener) future -> {
          channelQPSRollingNumber.increment(StatisticsTypeEnum.WRITE);
          if (!future.isSuccess()) {
            this.writeStatus = RequestWriteStatusEnum.FLUSH_ERROR;
            LOGGER.error("{}:channelWrite failed {} ",
                future.channel().remoteAddress(), future.cause().getMessage());
          }
          this.writeStatus = RequestWriteStatusEnum.FLUSH_SUCCESS;
          future.channel().close();
        });
    this.writeStatus = RequestWriteStatusEnum.WRITE;
    //释放资源
    releaseRequest();
  }

  private void writeFlow(ChannelHandlerContext ctx) {
    int retryCount = 0;
    while (!ctx.channel().isWritable() && ctx.channel().isActive()) {
      if (retryCount >= 8) {
        break;
      }
      try {
        Thread.sleep((long) (Math.pow(2, retryCount) * 10));
        LOGGER.info("触发高水位流量控制:{}", ctx.channel().unsafe().outboundBuffer().size());
      } catch (InterruptedException e) {
        retryCount = 8;
      }
      retryCount++;
    }
  }
}
