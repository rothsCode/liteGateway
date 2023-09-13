package com.rothsCode.liteGateway.core.register;

import com.rothsCode.liteGateway.core.config.RegisterConfig;
import com.rothsCode.liteGateway.core.register.nacos.NacosFetchService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description:
 * @date 2023/8/21 14:53
 */
@Slf4j
public class FetchServiceFactory {

  /**
   * 生成拉去类实例
   *
   * @param registerConfig
   * @return
   */
  public static FetchRegisterService create(RegisterConfig registerConfig) {
    RegisterTypeEnum registerTypeEnum = RegisterTypeEnum
        .getByCode(registerConfig.getRegisterType());
    switch (registerTypeEnum) {
      case EUREKA:
        return new EurekaFetchService(registerConfig);
      default:
        return new NacosFetchService(registerConfig);
    }
  }


}
