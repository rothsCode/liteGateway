package com.rothsCode.liteGateway.core.register;

/**
 * @author roths
 * @Description: 拉取注册信息
 * @date 2023/8/18 10:36
 */
public interface FetchRegisterService {


  /**
   * 拉去注册信息
   *
   * @param delta 是否增量拉取
   */
  void fetchRegisterService(boolean delta);


  /**
   * 注册中心类型
   *
   * @return
   */
  String registerType();

}
