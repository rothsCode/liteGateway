package com.rothsCode.liteGateway.core.nettyServer;

import com.rothsCode.liteGateway.core.config.NettyConfig;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description: 针对ssl连接进行流控
 * @date 2023/10/8 14:20
 */
@Sharable
@Slf4j
public class SslFlowControlHandler extends ChannelInboundHandlerAdapter {

  private NettyConfig nettyConfig;
  /**
   * ssl连接并发数
   */
  private AtomicLong sslCounter = new AtomicLong(0);

  public SslFlowControlHandler(NettyConfig nettyConfig) {
    this.nettyConfig = nettyConfig;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object paramObject) throws Exception {
    if (paramObject instanceof SslHandshakeCompletionEvent) {
      long sslCounterValue = sslCounter.incrementAndGet();
      //并发数达到限制则关闭连接控流
      if (sslCounterValue > nettyConfig.getSslShakeCounter()) {
        ctx.channel().close();
        sslCounter.decrementAndGet();
        log.info("sslCounterValue over:{}", sslCounterValue);
      }
    } else if (paramObject instanceof SslCloseCompletionEvent) {
      sslCounter.decrementAndGet();
    } else {
      super.userEventTriggered(ctx, paramObject);
    }
  }
}