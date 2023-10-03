package com.rothsCode.liteGateway.core.plugin.log.logCollector;

import cn.hutool.core.lang.Assert;
import com.lmax.disruptor.dsl.ProducerType;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import com.rothsCode.liteGateway.core.plugin.core.PluginManager;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.LogReporter;
import com.rothsCode.liteGateway.core.plugin.process.quene.DisruptorFlusher;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: 日志搜集器插件
 * @date 2023/8/29 20:42
 */
public class DefaultLogCollector implements LogCollector, Plugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLogCollector.class);

  private final AtomicBoolean startStatus = new AtomicBoolean(false);

  private static final String THREAD_NAME_PREFIX = "disruptor_log_collector-";

  /**
   * 缓存队列
   */
  private DisruptorFlusher<GatewayRequestLog> logCollectorDisruptor;

  private ServerConfig serverConfig;

  private LogReporter logReporter;

  @Override
  public boolean checkInit() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    return serverConfig.getLogCollectEnabled();
  }

  @Override
  public String pluginName() {
    return PluginEnum.LOG_COLLECT.getCode();
  }

  @Override
  public void init() {
    //获取具体的日志搜集客户端
    Plugin logPluginClient = PluginManager.getInstance()
        .getPluginByName(serverConfig.getLogClientType());
    Assert.notNull(logPluginClient, "日志客户端不存在");
    if (logPluginClient instanceof LogReporter) {
      logReporter = (LogReporter) logPluginClient;
    }
    Assert.notNull(logPluginClient, "日志客户端转化失败");

    //内存队列初始化
    DisruptorFlusher.Builder<GatewayRequestLog> builder = new DisruptorFlusher.Builder<GatewayRequestLog>()
        .setBufferSize(serverConfig.getLogBufferSize())
        .setThreads(serverConfig.getLogBufferThreadSize())
        .setProducerType(ProducerType.MULTI)
        .setNamePrefix(THREAD_NAME_PREFIX)
        .setWaitStrategy(serverConfig.getDisruptorWaitStrategy());
    //注册请求消费监听器
    DisruptorLogCollectEventListener disruptorEventProcessorListener = new DisruptorLogCollectEventListener();
    builder.setEventListener(disruptorEventProcessorListener);
    this.logCollectorDisruptor = builder.build();
    LOGGER.info("DefaultLogCollector has been initialized");
  }

  @Override
  public void start() {
    if (!startStatus.compareAndSet(false, true)) {
      return;
    }
    logCollectorDisruptor.start();
  }

  @Override
  public void shutDown() {
    if (logCollectorDisruptor != null) {
      logCollectorDisruptor.shutdown();
    }
    startStatus.set(false);
  }

  @Override
  public void batchCollectLog(List<GatewayRequestLog> requestLogs) {
    for (GatewayRequestLog gatewayRequestLog : requestLogs) {
      logCollectorDisruptor.add(gatewayRequestLog);
    }
  }

  @Override
  public void collectLog(GatewayRequestLog gatewayRequestLog) {
    logCollectorDisruptor.add(gatewayRequestLog);
  }


  /**
   * 消费日志事件
   */
  public class DisruptorLogCollectEventListener implements
      DisruptorFlusher.EventListener<GatewayRequestLog> {

    @Override
    public void onEvent(GatewayRequestLog gatewayRequestLog) {
      logReporter.reportLog(gatewayRequestLog);
    }

    @Override
    public void onException(Throwable ex, long sequence, GatewayRequestLog gatewayRequestLog) {
      LOGGER.error("DisruptorLogCollectEventListener consumeError:{},requestParam:{}", ex,
          gatewayRequestLog.toString());
    }
  }

}
