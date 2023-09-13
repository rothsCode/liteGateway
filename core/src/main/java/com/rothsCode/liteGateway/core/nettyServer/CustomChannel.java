package com.rothsCode.liteGateway.core.nettyServer;

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.config.ssl.HttpSslContextFactory;
import com.rothsCode.liteGateway.core.nettyServer.requestProcess.GatewayRequestHandler;
import com.rothsCode.liteGateway.core.util.RemotingHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description:nettychannel
 * @date 2023/8/6 21:54
 */
public class CustomChannel extends ChannelInitializer {

  private static final Logger log = LoggerFactory.getLogger(CustomChannel.class);

  private static final ChannelHandler GATEWAY_HANDLER_INSTANCE = new GatewayRequestHandler();

  @Override
  protected void initChannel(Channel channel) {
    //是否开启SSL
    ServerConfig serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    if (serverConfig.sslEnabled) {
      channel.pipeline()
          .addLast("sslHandler",
              new SslHandler(HttpSslContextFactory.createSSLEngine()));
    }
    CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials()
        .build();
    channel.pipeline()
        .addLast(new ProxyIPDecoder()//ip解码器
            , new HttpServerCodec() //http解码
            , new HttpObjectAggregator(65536)
            , new CorsHandler(corsConfig)
            , new IdleStateHandler(0, 0, 30)
            , new FlushConsolidationHandler(512)
            , GATEWAY_HANDLER_INSTANCE
        );

  }

  /**
   * 连接管理器
   */
  static class NettyServerConnectManagerHandler extends ChannelDuplexHandler {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
      final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      //log.debug("NETTY SERVER PIPLINE: channelRegistered {}", remoteAddr);
      super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
      final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      //log.debug("NETTY SERVER PIPLINE: channelUnregistered {}", remoteAddr);
      super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      //log.debug("NETTY SERVER PIPLINE: channelActive {}", remoteAddr);
      super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      //log.debug("NETTY SERVER PIPLINE: channelInactive {}", remoteAddr);
      super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state().equals(IdleState.ALL_IDLE)) {
          final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
          log.warn("NETTY SERVER PIPLINE: userEventTriggered: IDLE {}", remoteAddr);
          ctx.channel().close();
        }
      }
      ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
      final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      log.warn("NETTY SERVER PIPLINE: remoteAddr： {}, exceptionCaught {}", remoteAddr, cause);
      ctx.channel().close();
    }

  }
}
