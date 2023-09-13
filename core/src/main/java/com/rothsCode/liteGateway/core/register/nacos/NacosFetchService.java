package com.rothsCode.liteGateway.core.register.nacos;

import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.rothsCode.liteGateway.core.config.RegisterConfig;
import com.rothsCode.liteGateway.core.model.ServiceInfo;
import com.rothsCode.liteGateway.core.register.FetchRegisterService;
import com.rothsCode.liteGateway.core.register.RegisterTypeEnum;
import com.rothsCode.liteGateway.core.util.cache.ServiceCacheManger;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description: 拉取nacos服务注册信息
 * @date 2023/8/18 11:08
 */
@Slf4j
public class NacosFetchService implements FetchRegisterService {

  public NacosFetchService(RegisterConfig registerConfig) {
    NacosClient.getInstance().init(registerConfig);
  }

  public void fetchRegisterService(boolean delta) {
    int page = 1;
    int size = 100;
    int total = 100;
    while (page * size <= total) {
      //查询所有服务名
      ListView<String> listView = NacosClient.getInstance().getGroupAllInstances(page, size);
      total = listView.getCount();
      page++;
      for (String serviceName : listView.getData()) {
        List<Instance> instances = NacosClient.getInstance()
            .getAllInstancesByServiceName(serviceName);
        //缓存服务实例列表
        cacheLatestInstanceInfo(serviceName, instances);
        //对服务上下线进行监听
        NacosClient.getInstance().subscribe(serviceName, event -> {
          NamingEvent namingEvent = (NamingEvent) event;
          //对服务变动进行缓存更新
          cacheLatestInstanceInfo(serviceName, namingEvent.getInstances());
        });
      }
    }
  }

  @Override
  public String registerType() {
    return RegisterTypeEnum.NACOS.getCode();
  }

  private void cacheLatestInstanceInfo(String serviceName, List<Instance> instances) {
    List<ServiceInfo> serviceInfos = new ArrayList<>(4);
    for (Instance instance : instances) {
      ServiceInfo serviceInfo = ServiceInfo.builder()
          .serviceId(instance.getInstanceId())
          .serviceName(instance.getServiceName())
          .ip(instance.getIp())
          .port(instance.getPort())
          .metadata(instance.getMetadata())
          .build();
      serviceInfos.add(serviceInfo);
    }
    ServiceCacheManger.getInstance().putService(serviceName, serviceInfos);
  }


}
