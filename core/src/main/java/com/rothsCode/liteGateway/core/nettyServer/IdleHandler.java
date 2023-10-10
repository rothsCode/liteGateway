package com.rothsCode.liteGateway.core.nettyServer;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author roths
 * @Description: 心跳检测移除
 * @date 2023/10/8 14:20
 */
@Sharable
public class IdleHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object paramObject) throws Exception {
    if (paramObject instanceof IdleStateEvent) {
      IdleState state = ((IdleStateEvent) paramObject).state();
      if (state == IdleState.ALL_IDLE) {
        //关闭连接
        ctx.channel().close();
      }
    } else {
      super.userEventTriggered(ctx, paramObject);
    }
  }
}