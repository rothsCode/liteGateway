package com.rothsCode.liteGateway.core.plugin.core;

/**
 * @author roths
 * @Description: 插件生命周期
 * @date 2023/9/5 15:05
 */
public interface Plugin {


  /**
   * 检查插件是否允许初始化
   *
   * @return
   */
  boolean checkInit();

  /**
   * 插件名称
   */
  String pluginName();

  /**
   * 插件初始化
   */
  void init();

  /**
   * 插件启动
   */
  void start();

  /**
   * 优雅关闭
   */
  void shutDown();

}
