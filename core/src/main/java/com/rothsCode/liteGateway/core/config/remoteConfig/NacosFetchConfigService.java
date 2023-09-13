package com.rothsCode.liteGateway.core.config.remoteConfig;

import com.alibaba.nacos.api.config.listener.Listener;
import com.rothsCode.liteGateway.core.config.ConfigCenterConfig;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.register.nacos.NacosClient;
import java.util.concurrent.Executor;

/**
 * @author roths
 * @Description:
 * @date 2023/8/22 15:26
 */
public class NacosFetchConfigService implements FetchConfigService {

  public NacosFetchConfigService(ConfigCenterConfig configCenterConfig) {
    NacosClient.getInstance().initConfig(configCenterConfig);
  }

  @Override
  public String fetchConfig() {
    String content = NacosClient.getInstance().getConfig(new Listener() {
      @Override
      public Executor getExecutor() {
        return null;
      }

      @Override
      public void receiveConfigInfo(String configInfo) {
        GatewayConfigLoader.getInstance().loadRemoteJSONConfig(configInfo);
      }
    });
    GatewayConfigLoader.getInstance().loadRemoteJSONConfig(content);
    return content;
  }

  @Override
  public String configType() {
    return ConfigTypeEnum.NACOS.getCode();
  }
}
