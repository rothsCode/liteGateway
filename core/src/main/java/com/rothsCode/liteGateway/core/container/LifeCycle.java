package com.rothsCode.liteGateway.core.container;

/**
 * @author roths
 * @Description: 生命周期处理基类
 * @date 2023/8/6 19:05
 */
public interface LifeCycle {

  /**
   * 实例以及配置初始化
   */
  void init();

  /**
   * 实例启动
   */
  void start();

  /**
   * 优雅关闭
   */
  void shutDown();

}
