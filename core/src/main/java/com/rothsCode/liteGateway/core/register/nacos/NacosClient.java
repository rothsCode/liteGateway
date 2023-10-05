package com.rothsCode.liteGateway.core.register.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.rothsCode.liteGateway.core.config.ConfigCenterConfig;
import com.rothsCode.liteGateway.core.config.RegisterConfig;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description: nacos客户端api
 * @date 2023/8/16 17:28
 */
@Slf4j
public class NacosClient {

  private NamingService naming;

  private ConfigService configService;

  private RegisterConfig registerConfig;

  private ConfigCenterConfig configCenterConfig;

  private NacosClient() {

  }

  private static class SingletonHolder {

    private static final NacosClient INSTANCE = new NacosClient();
  }

  public static NacosClient getInstance() {
    return SingletonHolder.INSTANCE;
  }

  public void init(RegisterConfig registerConfig) {
    try {
      this.registerConfig = registerConfig;
      Properties properties = new Properties();
      properties.setProperty("serverAddr", registerConfig.getServerAddr());
      properties.setProperty("namespace", registerConfig.getNameSpace());
      naming = NamingFactory.createNamingService(properties);
    } catch (NacosException e) {
      log.error("nacos createService error:{}", e);
    }
  }

  public void initConfig(ConfigCenterConfig configCenterConfig) {
    try {
      this.configCenterConfig = configCenterConfig;
      Properties properties = new Properties();
      properties.setProperty("serverAddr", configCenterConfig.getServerAddr());
      properties.setProperty("namespace", configCenterConfig.getNameSpace());
      configService = NacosFactory.createConfigService(properties);
    } catch (NacosException e) {
      log.error("nacos createService error:{}", e);
    }
  }

  /**
   * 拉取配置信息
   *
   * @return
   */
  public String getConfig(Listener listener) {
    String content = null;
    try {
      content = configService
          .getConfig(configCenterConfig.getDataId(), configCenterConfig.getGroup(), 5000);
      //注册监听
      configService
          .addListener(configCenterConfig.getDataId(), configCenterConfig.getGroup(), listener);
    } catch (NacosException e) {
      log.error("nacos pull config fail:{}", e);
    }
    return content;
  }

  /**
   * 根据服务名获取服务所有实例
   *
   * @param serviceName
   * @return
   */
  public List<Instance> getAllInstancesByServiceName(String serviceName) {
    try {
      return naming.getAllInstances(serviceName, registerConfig.getGroup());
    } catch (NacosException e) {
      log.error("pull nacosInstance fair:{}", e);
    }
    return Collections.emptyList();
  }

  /**
   * 获取该集群下服务所有实例
   *
   * @param page
   * @param size
   * @return
   */
  public ListView<String> getGroupAllInstances(int page, int size) {
    try {
      return naming.getServicesOfServer(page, size, registerConfig.getGroup());
    } catch (NacosException e) {
      log.error("pull nacosInstance fair:{}", e);
    }
    return new ListView<>();
  }

  /**
   * 监听服务上下线
   */
  public void subscribe(String serviceName, EventListener listener) {
    try {
      naming.subscribe(serviceName, registerConfig.getGroup(), event -> {
        if (event instanceof NamingEvent) {
          log.info("{}服务发生变动:{}", serviceName, event.toString());
          listener.onEvent(event);
        }
      });
    } catch (NacosException e) {
      log.error("subscribe nacosInstance fair:{}", e);
    }
  }




}
