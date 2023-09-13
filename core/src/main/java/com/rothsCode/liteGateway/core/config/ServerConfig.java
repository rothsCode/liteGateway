package com.rothsCode.liteGateway.core.config;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import lombok.Data;

/**
 * @author roths
 * @Description:服务端配置类
 * @date 2023/8/6 19:01
 */
@Data
public class ServerConfig {

  /**
   * 是否开启ssl
   */
  public Boolean sslEnabled = false;
  /**
   * ssl证书存储路径
   */
  public String keystorePath;
  /**
   * 证书认证密码
   */
  public String certificatePassword;
  /**
   * 证书存储密码
   */
  public String keystorePassword;
  /**
   * ssl证书类型
   */
  public String keyStoreType;
  //注册中心配置
  private RegisterConfig registerConfig;
  //配置中心配置
  private ConfigCenterConfig configCenterConfig;

  //	Http Async 参数选项：
  //netty服务端配置
  private NettyConfig nettyConfig;
  //cache
  private Long cacheMaxSize = 1024L;
  //过期时间 单位秒
  private Long expireTime = 1000L;
  /**
   * 项目名
   */
  private String applicationName;
  /**
   * http客户端
   */
  private String httpClientType = PluginEnum.ASYNC_NETTY_HTTP_CLIENT.getCode();
  //	连接超时时间
  private int httpConnectTimeout = 10 * 1000;
  //	请求超时时间
  private int httpRequestTimeout = 10 * 1000;
  //	客户端请求重试次数
  private int httpMaxRequestRetry = 2;
  //	客户端请求最大连接数
  private int httpMaxConnections = 100000;
  //	客户端每个地址支持的最大连接数
  private int httpConnectionsPerHost = 80000;
  //	客户端空闲连接超时时间, 默认60秒
  private int httpPooledConnectionIdleTimeout = 60 * 1000;
  /**
   * 请求处理器类型 默认disruptorProcess
   */
  private String requestProcessType;
  /**
   * 处理器队列大小
   */
  private int processBufferSize = 1024 * 32;
  /**
   * 处理器使用的线程数
   */
  private int processThreadSize = Runtime.getRuntime().availableProcessors();
  /**
   * 处理器等待模式
   */
  private String processWaitStrategy = "blocking";
  /**
   * redis模式  standalone sentinel  cluster
   */
  private String redisModel;
  /**
   * redis地址
   */
  private String redisAddress;
  /**
   * redis密码
   */
  private String redisPassword;
  /**
   * 开启metrics
   */
  private Boolean metricsEnabled = false;
  /**
   * metrics类型
   */
  private String metricsType = "prometheus";
  /**
   * 开启prometheus jvm监控
   */
  private Boolean jvmEnabled;
  /**
   * prometheus host
   */
  private String prometheusHost;
  /**
   * prometheus port
   */
  private int prometheusPort;
  /**
   * 开启日志收集
   */
  private Boolean logCollectEnabled = true;
  /**
   * 日志容器大小
   */
  private int logQueueSize = 512;
  /**
   * 日志搜集客户端类型
   */
  private String logClientType = "fileStorage";
  /**
   * 日志搜集客户端连接地址
   */
  private String logClientAddress;
  /**
   * 日志消息topic
   */
  private String logTopic;
  /**
   * 日志文件存储路径
   */
  private String fileStorePath = "/log";
  /**
   * 日志文件名
   */
  private String logFileName = "gateway";
  /**
   * 是否只记录错误日志
   */
  private Boolean onlyAppenderError = true;
  /**
   * 是否开启权限校验
   */
  private Boolean authEnabled = false;
  /**
   * 权限校验类型
   */
  private String authType;

  public WaitStrategy getDisruptorWaitStrategy() {
    switch (processWaitStrategy) {
      case "blocking":
        return new BlockingWaitStrategy();
      case "busySpin":
        return new BusySpinWaitStrategy();
      case "yielding":
        return new YieldingWaitStrategy();
      case "sleeping":
        return new SleepingWaitStrategy();
      default:
        return new BlockingWaitStrategy();
    }
  }
}
