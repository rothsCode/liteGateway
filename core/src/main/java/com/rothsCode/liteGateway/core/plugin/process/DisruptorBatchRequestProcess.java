package com.rothsCode.liteGateway.core.plugin.process;

import com.lmax.disruptor.dsl.ProducerType;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.model.Result;
import com.rothsCode.liteGateway.core.nettyServer.requestProcess.NettyDefaultRequestProcess;
import com.rothsCode.liteGateway.core.nettyServer.requestProcess.NettyRequestProcess;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import com.rothsCode.liteGateway.core.plugin.process.quene.DisruptorFlusher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author roths
 * @Description: 基于disruptor的高性能处理器
 * @date 2023/8/13 11:27
 */
public class DisruptorBatchRequestProcess implements NettyRequestProcess, Plugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(DisruptorBatchRequestProcess.class);

  private static final String THREAD_NAME_PREFIX = "disruptor_process-";

  private final AtomicBoolean startStatus = new AtomicBoolean(false);

  private ServerConfig serverConfig;

  private DisruptorFlusher<GatewayContext> disruptorFlusher;

  private NettyDefaultRequestProcess defaultRequestProcess;

  @Override
  public void processRequest(GatewayContext gatewayContext) {
    disruptorFlusher.add(gatewayContext);
  }

  @Override
  public boolean checkInit() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    return PluginEnum.DISRUPTOR_PROCESS.getCode().equals(serverConfig.getRequestProcessType());
  }

  @Override
  public String pluginName() {
    return PluginEnum.DISRUPTOR_PROCESS.getCode();
  }

  @Override
  public void init() {
    //默认请求处理器
    defaultRequestProcess = new NettyDefaultRequestProcess();
    //内存队列初始化
    DisruptorFlusher.Builder<GatewayContext> builder = new DisruptorFlusher.Builder<GatewayContext>()
        .setBufferSize(serverConfig.getProcessBufferSize())
        .setThreads(serverConfig.getProcessThreadSize())
        .setProducerType(ProducerType.MULTI)
        .setNamePrefix(THREAD_NAME_PREFIX)
        .setWaitStrategy(serverConfig.getDisruptorWaitStrategy());
    //注册请求消费监听器
    DisruptorEventProcessorListener disruptorEventProcessorListener = new DisruptorEventProcessorListener();
    builder.setEventListener(disruptorEventProcessorListener);
    this.disruptorFlusher = builder.build();
    LOGGER.info(" disruptorBatchRequestProcess has been initialized");
  }

  @Override
  public void start() {
    if (!startStatus.compareAndSet(false, true)) {
      return;
    }
    disruptorFlusher.start();
  }

  @Override
  public void shutDown() {
    startStatus.set(false);
    if (disruptorFlusher != null) {
      disruptorFlusher.shutdown();
    }
  }

  /**
   * 请求消费事件
   */
  public class DisruptorEventProcessorListener implements
      DisruptorFlusher.EventListener<GatewayContext> {

    @Override
    public void onEvent(GatewayContext event) {
      defaultRequestProcess.processRequest(event);
    }

    @Override
    public void onException(Throwable ex, long sequence, GatewayContext gatewayContext) {
      LOGGER.error("DisruptorEventProcessorListener consumeError:{},requestParam:{}", ex,
          gatewayContext);
      ByteBuf byteBuf = Unpooled
          .wrappedBuffer(
              Result.errorGatewayError(GatewayRequestStatusEnum.REQUEST_PROCESS_ERROR.getDesc())
                  .toString().getBytes());
      FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
          HttpResponseStatus.valueOf(HttpStatus.SC_BAD_GATEWAY),
          byteBuf);
      gatewayContext.writeResponse(httpResponse);
    }
  }
}
