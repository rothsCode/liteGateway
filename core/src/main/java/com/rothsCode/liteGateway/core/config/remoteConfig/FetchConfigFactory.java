package com.rothsCode.liteGateway.core.config.remoteConfig;

import com.rothsCode.liteGateway.core.config.ConfigCenterConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description:
 * @date 2023/8/21 14:53
 */
@Slf4j
public class FetchConfigFactory {

  /**
   * 生成拉去类实例
   *
   * @param configCenterConfig
   * @return
   */
  public static FetchConfigService create(ConfigCenterConfig configCenterConfig) {
    ConfigTypeEnum configTypeEnum = ConfigTypeEnum
        .getByCode(configCenterConfig.getConfigType());
    switch (configTypeEnum) {
      case APOLLO:
        return null;
      default:
        return new NacosFetchConfigService(configCenterConfig);
    }
  }


}
