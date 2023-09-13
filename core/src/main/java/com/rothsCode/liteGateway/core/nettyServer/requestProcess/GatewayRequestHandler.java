package com.rothsCode.liteGateway.core.nettyServer.requestProcess;

import com.rothsCode.liteGateway.core.Context.GatewayContext;
import com.rothsCode.liteGateway.core.Context.GatewayRequest;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.plugin.core.PluginManager;
import com.rothsCode.liteGateway.core.plugin.metrics.memoryMetrics.StatisticsRollingNumber;
import com.rothsCode.liteGateway.core.plugin.metrics.remoteMetrics.StatisticsTypeEnum;
import com.rothsCode.liteGateway.core.util.IPUtils;
import com.rothsCode.liteGateway.core.util.ThreadFactoryImpl;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author roths
 * @Description:业务处理类基类
 * @date 2023/8/6 21:57
 */
@Sharable
@Slf4j
public class GatewayRequestHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(GatewayRequestHandler.class);
  private static final String CHANNEL_ROLLING_NUMBER = "CHANNEL_QPS";
  private final ScheduledExecutorService qpsExecutor =
      Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("channelQPSExecutor"));
  private ServerConfig serverConfig;
  private NettyRequestProcess nettyRequestProcess;
  private StatisticsRollingNumber channelQPSRollingNumber;

  public GatewayRequestHandler() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    //获取请求处理器
    nettyRequestProcess = (NettyRequestProcess) PluginManager.getInstance()
        .getPluginByName(serverConfig.getRequestProcessType());
    //没有则使用默认处理器
    if (nettyRequestProcess == null) {
      nettyRequestProcess = new NettyDefaultRequestProcess();
    }
    //进行读写qps统计
    channelQPSRollingNumber = new StatisticsRollingNumber(
        CHANNEL_ROLLING_NUMBER, 10 * 1000, 10);
    qpsExecutor.scheduleAtFixedRate(() -> {
      if (channelQPSRollingNumber.getRollingSum(StatisticsTypeEnum.READ) > 0) {
        log.info("channelReadQps:{}",
            channelQPSRollingNumber.getValues(StatisticsTypeEnum.READ));
        log.info("channelWriteQps:{}",
            channelQPSRollingNumber.getValues(StatisticsTypeEnum.WRITE));
      }
    }, 10, 10, TimeUnit.SECONDS);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    channelQPSRollingNumber.increment(StatisticsTypeEnum.READ);
    //根据不同请求类型匹配不同处理器
    if (msg instanceof FullHttpRequest) {
      //http请求
      GatewayRequest request = new GatewayRequest();
      request.setCtx(ctx);
      request.setMsg(msg);
      request.setChannelQPSRollingNumber(channelQPSRollingNumber);
      GatewayContext gatewayContext = new GatewayContext();
      gatewayContext.setGatewayRequest(request);
      gatewayContext.setStartTime(System.currentTimeMillis());
      gatewayContext
          .setLoadBalanceStrategy(serverConfig.getRegisterConfig().getLoadBalanceStrategy());
      gatewayContext.setClientIP(IPUtils.getClientIP(ctx, (HttpRequest) msg));
      nettyRequestProcess.processRequest(gatewayContext);
    } else {
      log.error("not httpRequest:{}", msg.toString());
      boolean releaseFlag = ReferenceCountUtil.release(msg);
      if (!releaseFlag) {
        log.error("reference Fair:{}", msg.toString());
      }
    }
  }


  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    ctx.close();
    LOGGER.error("发生错误:{}" + cause.getMessage());

  }

}
