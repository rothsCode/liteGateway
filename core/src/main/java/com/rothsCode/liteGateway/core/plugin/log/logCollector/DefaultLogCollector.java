package com.rothsCode.liteGateway.core.plugin.log.logCollector;

import cn.hutool.core.lang.Assert;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import com.rothsCode.liteGateway.core.plugin.core.PluginManager;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.LogReporter;
import com.rothsCode.liteGateway.core.util.ThreadFactoryImpl;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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
  /**
   * 缓存队列 TODO 后期优化为disruptor
   */
  private BlockingQueue<GatewayRequestLog> logBlockingQueue;
  private ExecutorService collectLogService;
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
    Plugin logPluginClient = PluginManager.getInstance()
        .getPluginByName(serverConfig.getLogClientType());
    Assert.notNull(logPluginClient, "日志客户端不存在");
    if (logPluginClient instanceof LogReporter) {
      logReporter = (LogReporter) logPluginClient;
    }
    Assert.notNull(logPluginClient, "日志客户端转化失败");
    //如果gc压力较大可改用ArrayBlockingQueue降低gc压力，同时吞吐量会下降，
    logBlockingQueue = new LinkedBlockingQueue<>(serverConfig.getLogQueueSize());
    collectLogService = Executors
        .newSingleThreadExecutor(new ThreadFactoryImpl("collectLogServiceThread"));
    LOGGER.info("DefaultLogCollector has init");
  }

  @Override
  public void start() {
    if (!startStatus.compareAndSet(false, true)) {
      return;
    }
    collectLogService.execute(this::consumeQueue);
  }

  public void consumeQueue() {
    while (startStatus.get()) {
      try {
        GatewayRequestLog gatewayRequestLog = logBlockingQueue.take();
        logReporter.reportLog(gatewayRequestLog);
        //获取具体的日志搜集客户端
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

  }

  @Override
  public void shutDown() {
    logBlockingQueue.clear();
    collectLogService.shutdown();
  }

  @Override
  public void batchCollectLog(List<GatewayRequestLog> requestLogs) {
    logBlockingQueue.addAll(requestLogs);
  }

  @Override
  public void collectLog(GatewayRequestLog gatewayRequestLog) {
    logBlockingQueue.add(gatewayRequestLog);
  }
}
