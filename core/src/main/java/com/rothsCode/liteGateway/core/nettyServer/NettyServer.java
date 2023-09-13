package com.rothsCode.liteGateway.core.nettyServer;

import com.rothsCode.liteGateway.core.config.NettyConfig;
import com.rothsCode.liteGateway.core.container.LifeCycle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author roths
 * @Description: nettyserver启动类
 * @date 2023/8/6 18:57
 */
public class NettyServer implements LifeCycle {

  private static final Logger log = LoggerFactory.getLogger(NettyServer.class);
  public EventLoopGroup bossGroup;

  public EventLoopGroup workGroup;

  public ServerBootstrap serverBootstrap;

  private NettyConfig nettyConfig;

  private CustomChannel customChannel;


  public NettyServer(NettyConfig nettyConfig) {
    this.nettyConfig = nettyConfig;
  }

  public void setCustomChannel(CustomChannel customChannel) {
    this.customChannel = customChannel;
  }

  @Override
  public void init() {
    serverBootstrap = new ServerBootstrap();
    if (nettyConfig.isLinux() && Epoll.isAvailable()) {
      log.info("epoll BossThreadCount:{},WorkThreadCount:{}", nettyConfig.getBossThreadCount(),
          nettyConfig.getWorkThreadCount());
      bossGroup = new EpollEventLoopGroup(nettyConfig.getBossThreadCount(),
          new DefaultThreadFactory("nettyEpollBossGroup"));
      workGroup = new EpollEventLoopGroup(nettyConfig.getWorkThreadCount(),
          new DefaultThreadFactory("nettyEpollWorkGroup"));
    } else {
      log.info("nio BossThreadCount:{},WorkThreadCount:{}", nettyConfig.getBossThreadCount(),
          nettyConfig.getWorkThreadCount());
      bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreadCount(),
          new DefaultThreadFactory("nettyNIOBossGroup"));
      workGroup = new NioEventLoopGroup(nettyConfig.getWorkThreadCount(),
          new DefaultThreadFactory("nettyNIOworkGroup"));
    }
    nettyConfig.setWorkGroup(workGroup);
  }

  @Override
  public void start() {
    ServerBootstrap server = serverBootstrap.group(bossGroup, workGroup)
        .channel(nettyConfig.isLinux() && Epoll.isAvailable() ? EpollServerSocketChannel.class
            : NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 2048)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.SO_SNDBUF, 65535) //发送请求大小
        .childOption(ChannelOption.SO_RCVBUF, 65535) //接收请求大小
        .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
            new WriteBufferWaterMark(64 * 1024, 256 * 1024)) //高低水位设置
        .childOption(ChannelOption.TCP_NODELAY, true)//发送等待
        .childOption(ChannelOption.SO_REUSEADDR, true) //端口复用
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//内存池
        .localAddress(new InetSocketAddress(nettyConfig.getPort()))
        .childHandler(customChannel);
    try {
      ChannelFuture channelFuture = server.bind().sync();
      log.info("----nettyServer:{} has started---", nettyConfig.getPort());
      channelFuture.channel().closeFuture().sync();
    } catch (Exception e) {
      log.error("----nettyServer start failed:{}---", e);
    } finally {
      bossGroup.shutdownGracefully();
      workGroup.shutdownGracefully();
    }


  }

  @Override
  public void shutDown() {
    if (bossGroup != null) {
      bossGroup.shutdownGracefully();
      log.info("bossGroup优雅关闭");
    }
    if (workGroup != null) {
      workGroup.shutdownGracefully();
      log.info("workGroup优雅关闭");
    }
  }
}
