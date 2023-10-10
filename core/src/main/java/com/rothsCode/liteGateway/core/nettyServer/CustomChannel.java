package com.rothsCode.liteGateway.core.nettyServer;

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.config.ssl.SSLContextFactory;
import com.rothsCode.liteGateway.core.nettyServer.requestProcess.GatewayRequestHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description:nettychannel
 * @date 2023/8/6 21:54
 */
public class CustomChannel extends ChannelInitializer {

  private static final Logger log = LoggerFactory.getLogger(CustomChannel.class);
  /**
   * 请求业务线程池,和channel绑定减少锁竞争,与单条链路1对1关系 如只有链路则并发失效
   */
  private static DefaultEventExecutorGroup gatewayRequestExecutorGroup =
      new DefaultEventExecutorGroup((Runtime.getRuntime().availableProcessors() * 2));

  @Override
  protected void initChannel(Channel channel) {
    //是否开启SSL
    ServerConfig serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    if (serverConfig.getSslConfig().isSslEnabled()) {
      channel.pipeline()
          .addLast("sslHandler",
              SSLContextFactory.getOpenSslHandler(
                  serverConfig.getSslConfig(), channel.alloc()))
          .addLast("SslFlowControlHandler",
              new SslFlowControlHandler(serverConfig.getNettyConfig()));
    }
    CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials()
        .build();
    channel.pipeline()
        .addLast(new ProxyIPDecoder()//ip解码器
            , new HttpServerCodec() //http解码
            , new HttpObjectAggregator(65536)//半包处理
            , new CorsHandler(corsConfig)
            , new IdleStateHandler(0, 0, serverConfig.getNettyConfig().getIdleTimeOutSeconds())
            , new IdleHandler()//心跳检测
            , new FlushConsolidationHandler(512)//batchFlush
        );
    channel.pipeline()
        .addLast(gatewayRequestExecutorGroup, "gatewayRequestHandler", new GatewayRequestHandler());
  }


}
