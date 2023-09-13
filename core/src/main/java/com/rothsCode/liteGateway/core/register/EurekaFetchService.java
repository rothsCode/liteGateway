package com.rothsCode.liteGateway.core.register;


import com.rothsCode.liteGateway.core.config.RegisterConfig;

/**
 * @author roths
 * @Description:eureka 注册信息拉取类
 * @date 2023/8/18 11:09
 */
public class EurekaFetchService implements FetchRegisterService {

  public EurekaFetchService(RegisterConfig registerConfig) {

  }

  @Override
  public void fetchRegisterService(boolean delta) {

  }

  @Override
  public String registerType() {
    return RegisterTypeEnum.EUREKA.getCode();
  }
}
