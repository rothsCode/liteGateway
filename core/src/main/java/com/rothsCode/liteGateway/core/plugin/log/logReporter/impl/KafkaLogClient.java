package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import com.rothsCode.liteGateway.core.plugin.log.logCollector.GatewayRequestLog;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.LogReporter;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: kafka日志搜集客户端
 * @date 2023/9/5 13:59
 */
public class KafkaLogClient<T> implements Plugin, LogReporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaLogClient.class);
  /**
   * 每个发送批次大小为 16K
   **/
  private final int batchSize = 1024 * 16;

  /**
   * batch 没满的情况下默认等待 100 ms
   **/
  private final int lingerMs = 100;

  /**
   * producer 的缓存为 64M
   **/
  private final int bufferMemory = 1024 * 1024 * 64;

  /**
   * 需要确保写入副本 leader
   **/
  private final String acks = "1";

  /**
   * 为了减少带宽，使用 lz4 压缩
   **/
  private final String compressionType = "lz4";

  /**
   * 当 memory buffer 满了之后，send() 在抛出异常之前阻塞的最长时间
   **/
  private final int blockMs = 10000;

  private final String serializerClass = "org.apache.kafka.common.serialization.StringSerializer";

  private Properties props;

  private KafkaProducer<String, String> producer;

  private String topic;

  private ServerConfig serverConfig;


  @Override
  public boolean checkInit() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    return PluginEnum.KAFKA.getCode().equals(serverConfig.getLogClientType());
  }

  @Override
  public String pluginName() {
    return PluginEnum.KAFKA.getCode();
  }

  @Override
  public void init() {
    Assert.notNull(serverConfig.getLogTopic(), "logTopic为空");
    Assert.notNull(serverConfig.getLogClientAddress(), "logClientAddress为空");
    topic = serverConfig.getLogTopic();
    props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverConfig.getLogClientAddress());
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
    props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
    props.put(ProducerConfig.ACKS_CONFIG, acks);
    props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, blockMs);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializerClass);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializerClass);
    LOGGER.info("KafkaLogClient has been initialized");
  }

  /**
   * 异步发送
   *
   * @param message
   */
  public void sendAsync(T message) {
    Objects.requireNonNull(topic);
    Objects.requireNonNull(message);
    producer.send(new ProducerRecord<>(topic, JSON.toJSONString(message)));
  }

  /**
   * 异步发送带回调函数
   *
   * @param message
   * @param callback
   */
  public void sendAsyncCallback(T message, Callback callback) {
    Objects.requireNonNull(topic);
    Objects.requireNonNull(message);
    producer.send(new ProducerRecord<>(topic, JSON.toJSONString(message)), callback);
  }

  @Override
  public void start() {
    this.producer = new KafkaProducer<>(props);
  }

  @Override
  public void shutDown() {
    this.producer.close();
  }

  @Override
  public void reportLog(GatewayRequestLog gatewayRequestLog) {
    sendAsync((T) gatewayRequestLog);
  }
}
