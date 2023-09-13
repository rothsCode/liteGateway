package com.rothsCode.liteGateway.core.util.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rothsCode.liteGateway.core.model.ServiceInfo;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author roths
 * @Description:服务实例缓存类 基于caffee缓存组件
 * @date 2023/8/16 18:08
 */
public class ServiceCacheManger {

  private static Cache<String, List<ServiceInfo>> serviceMap = Caffeine.newBuilder()
      .maximumSize(1024)
      .expireAfterWrite(30, TimeUnit.MINUTES)
      .build();

  private ServiceCacheManger() {
  }

  /***************** 	对服务定义缓存进行操作的系列方法 	***************/

  public static ServiceCacheManger getInstance() {
    return SingletonHolder.INSTANCE;
  }

  public void putService(String serviceName, List<ServiceInfo> serviceInfos) {
    serviceMap.put(serviceName, serviceInfos);
  }

  public void removeService(String serviceName) {
    serviceMap.invalidate(serviceName);
  }

  public List<ServiceInfo> getServiceList(String serviceName) {
    return serviceMap.getIfPresent(serviceName);
  }

  public static class SingletonHolder {

    private static final ServiceCacheManger INSTANCE = new ServiceCacheManger();
  }
}
