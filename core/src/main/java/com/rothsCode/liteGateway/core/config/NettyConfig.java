package com.rothsCode.liteGateway.core.config;

import io.netty.channel.EventLoopGroup;
import lombok.Data;

/**
 * @author roths
 * @Description:
 * @date 2023/8/6 19:01
 */
@Data
public class NettyConfig {

  public EventLoopGroup workGroup;
  private boolean isUseEpoll;
  private int port = 9000;
  private int bossThreadCount = 1;
  private int workThreadCount;
  /**
   * 业务处理线程
   */
  private int serviceThreadCount = Runtime.getRuntime().availableProcessors() * 2;

  public boolean isLinux() {
    return System.getProperty("os.name").toLowerCase().contains("linux");
  }

  public boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("windows");
  }

}
