package com.rothsCode.liteGateway.core.config.remoteConfig;

/**
 * @author roths
 * @Description: 拉取注册信息
 * @date 2023/8/18 10:36
 */
public interface FetchConfigService {


  /**
   * 拉去注册信息
   */
  String fetchConfig();


  /**
   * 注册中心类型
   *
   * @return
   */
  String configType();

}
