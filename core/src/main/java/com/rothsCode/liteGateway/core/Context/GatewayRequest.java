package com.rothsCode.liteGateway.core.Context;

import com.rothsCode.liteGateway.core.plugin.metrics.memoryMetrics.StatisticsRollingNumber;
import com.rothsCode.liteGateway.core.plugin.metrics.remoteMetrics.StatisticsTypeEnum;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: 网关请求
 * @date 2023/8/13 16:34
 */
@Data
public class GatewayRequest {

  private static final Logger LOGGER = LoggerFactory.getLogger(GatewayRequest.class);
  /**
   * channel上下文
   */
  private ChannelHandlerContext ctx;

  /**
   * 请求业务信息
   */
  private Object msg;

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
    //水位控制
    writeFlow(ctx);
    ctx.writeAndFlush(httpResponse).addListener((ChannelFutureListener) future -> {
      channelQPSRollingNumber.increment(StatisticsTypeEnum.WRITE);
//      if (!future.isSuccess()) {
//        LOGGER.error("{}:channelWrite failed {} ",
//            future.channel().remoteAddress(), future.cause().getMessage());
//      }
      future.channel().close();
    });
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
