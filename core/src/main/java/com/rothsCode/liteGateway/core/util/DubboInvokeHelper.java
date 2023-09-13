package com.rothsCode.liteGateway.core.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.RegisterConfig;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.model.DubboServiceInvoker;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * @author roths
 * @Description: dubbo泛化调用工具类 缓存化泛化调用实例
 * @date 2023/8/22 18:09
 */
public class DubboInvokeHelper {

  private static Cache<String, GenericService> GENERIC_SERVICE_CACHE = Caffeine.newBuilder()
      .build();

  public static GenericService getGenericService(ServerConfig serverConfig,
      DubboServiceInvoker dubboServiceInvoker) {
    RegisterConfig registerConfig = serverConfig.getRegisterConfig();
    String dubboServiceKey =
        registerConfig.getServerAddr() + ":" + dubboServiceInvoker.getInterfaceName()
            + ":" + dubboServiceInvoker.getVersion();
    GenericService genericService = GENERIC_SERVICE_CACHE.getIfPresent(dubboServiceKey);
    if (genericService == null) {
      ReferenceConfig<GenericService> referenceConfig = DubboInvokeHelper.
          buildReferenceConfig(GatewayConfigLoader.getInstance().getServerConfig(),
              dubboServiceInvoker);
      //获取服务
      genericService = referenceConfig.get();
    }
    return genericService;
  }

  public static ReferenceConfig<GenericService> buildReferenceConfig(ServerConfig serverConfig,
      DubboServiceInvoker dubboServiceInvoker) {
    RegisterConfig registerConfig = serverConfig.getRegisterConfig();
    ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
    ApplicationConfig applicationConfig = new ApplicationConfig();
    applicationConfig.setName(serverConfig.getApplicationName());
    RegistryConfig registryConfig = new RegistryConfig();
    registryConfig.setCheck(true);
    registryConfig.setTimeout(10000);
    registryConfig.setProtocol(registerConfig.getRegisterType());
    registryConfig.setAddress(registerConfig.getServerAddr());
    registryConfig.setGroup(registerConfig.getGroup());
    registryConfig.setUsername(registerConfig.getUserName());
    registryConfig.setPassword(registerConfig.getPassword());
    referenceConfig.setApplication(applicationConfig);
    referenceConfig.setRegistry(registryConfig);
    referenceConfig.setInterface(dubboServiceInvoker.getInterfaceName());
    referenceConfig.setVersion(dubboServiceInvoker.getVersion());
    referenceConfig.setGeneric(Boolean.TRUE.toString());
    referenceConfig.setCheck(false);
    return referenceConfig;
  }

}
