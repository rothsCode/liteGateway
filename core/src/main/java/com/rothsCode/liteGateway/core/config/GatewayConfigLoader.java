package com.rothsCode.liteGateway.core.config;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.rothsCode.liteGateway.core.config.remoteConfig.GatewayDyamicConfig;
import com.rothsCode.liteGateway.core.config.ssl.SSLConfig;
import com.rothsCode.liteGateway.core.model.DubboRouteRule;
import com.rothsCode.liteGateway.core.model.FlowRule;
import com.rothsCode.liteGateway.core.model.ProxyRouteRule;
import com.rothsCode.liteGateway.core.model.ServiceRouteRule;
import com.rothsCode.liteGateway.core.pipeline.rateLimiter.FlowControlManager;
import com.rothsCode.liteGateway.core.util.PropertiesUtils;
import com.rothsCode.liteGateway.core.util.radixTree.TextRadixTree;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author roths
 * @Description: 配置加载类
 * @date 2023/8/17 19:48
 */
@Slf4j
public class GatewayConfigLoader {

  private static final GatewayConfigLoader configLoader = new GatewayConfigLoader();
  private static final String LOCAL_CONFIG_FILE = "/application-local.properties";
  private static final String DEV_CONFIG_FILE = "/application-dev.properties";
  private static final String PRO_CONFIG_FILE = "/application-pro.properties";
  /**
   * 网关配置信息
   */
  private ServerConfig serverConfig;
  //动态配置信息
  private GatewayDyamicConfig gatewayDyamicConfig;

  public GatewayConfigLoader() {
  }

  public static GatewayConfigLoader getInstance() {
    return configLoader;
  }

  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  public GatewayDyamicConfig getGatewayDyamicConfig() {
    return gatewayDyamicConfig;
  }

  /**
   * 配置中心动态更新
   *
   * @return
   */
  public ServerConfig setServerConfig(ServerConfig serverConfig) {
    return this.serverConfig = serverConfig;
  }

  public ServerConfig loadConfig() {
    String configFile = LOCAL_CONFIG_FILE;
    String env = System.getenv("profile");
    if ("dev".equals(env)) {
      configFile = DEV_CONFIG_FILE;
    } else if ("pro".equals(env)) {
      configFile = PRO_CONFIG_FILE;
    }
    //加载配置文件
    InputStream inputStream = GatewayConfigLoader.class.getResourceAsStream(configFile);
    if (inputStream != null) {
      Properties properties = new Properties();
      try {
        properties.load(inputStream);
        ServerConfig serverConfig = new ServerConfig();
        //网关基础配置
        PropertiesUtils.properties2Object(properties, serverConfig, "gateway");
        //netty配置
        NettyConfig nettyConfig = new NettyConfig();
        PropertiesUtils.properties2Object(properties, nettyConfig, "netty");
        serverConfig.setNettyConfig(nettyConfig);
        //注册中心配置
        RegisterConfig registerConfig = new RegisterConfig();
        PropertiesUtils.properties2Object(properties, registerConfig, "registerCenter");
        serverConfig.setRegisterConfig(registerConfig);
        //配置中心配置
        ConfigCenterConfig configCenterConfig = new ConfigCenterConfig();
        PropertiesUtils.properties2Object(properties, configCenterConfig, "configCenter");
        serverConfig.setConfigCenterConfig(configCenterConfig);
        //ssl证书配置
        SSLConfig sslConfig = new SSLConfig();
        PropertiesUtils.properties2Object(properties, sslConfig, "ssl");
        serverConfig.setSslConfig(sslConfig);
        //网关动态配置
        GatewayDyamicConfig gatewayDyamicConfig = new GatewayDyamicConfig();
        PropertiesUtils.properties2Object(properties, gatewayDyamicConfig, "dynamic");
        this.gatewayDyamicConfig = gatewayDyamicConfig;
        this.serverConfig = serverConfig;
        return serverConfig;
      } catch (IOException e) {
        log.error("load gatewayConfig error:{}", e);
      } finally {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.error("load gatewayConfig close error:{}", e);
        }
      }
    }
    return null;
  }

  public ServerConfig loadRemotePropertiesConfig(String content) {
    if (StringUtils.isBlank(content)) {
      return null;
    }
    InputStream inputStream = null;
    try {
      inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
      Properties properties = new Properties();
      properties.load(inputStream);
      //网关动态配置
      GatewayDyamicConfig gatewayDyamicConfig = new GatewayDyamicConfig();
      PropertiesUtils.properties2Object(properties, gatewayDyamicConfig, "gateway");
    } catch (IOException e) {
      log.error("load remoteConfig error:{}", e);
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        log.error("load remoteConfig close error:{}", e);
      }
    }
    return serverConfig;
  }

  /**
   * 全量删除更新
   *
   * @param content
   * @return
   */
  public synchronized GatewayDyamicConfig loadRemoteJSONConfig(String content) {
    if (StringUtils.isEmpty(content)) {
      return new GatewayDyamicConfig();
    }
    GatewayDyamicConfig gatewayDyamicConfig = JSONObject
        .parseObject(content, GatewayDyamicConfig.class);
    //dubbo路由配置填充至radixTree
    if (CollectionUtil.isNotEmpty(gatewayDyamicConfig.getDubboRouteRules())) {
      TextRadixTree<DubboRouteRule> tempDubboRouteRadixTree = new TextRadixTree();
      gatewayDyamicConfig.getDubboRouteRules().forEach(p -> {
        tempDubboRouteRadixTree.put(p.getApiPath(), p);
      });
      gatewayDyamicConfig.setDubboRouteRadixTree(tempDubboRouteRadixTree);
      gatewayDyamicConfig.setDubboRouteRules(null);
    }
    //服务路由配置-填充至radixTree
    if (CollectionUtil.isNotEmpty(gatewayDyamicConfig.getHttpServiceRouteRules())) {
      TextRadixTree<ServiceRouteRule> tempHttpServiceRouteRadixTree = new TextRadixTree();
      gatewayDyamicConfig.getHttpServiceRouteRules().forEach(p -> {
        tempHttpServiceRouteRadixTree.put(p.getApiPath(), p);
      });
      gatewayDyamicConfig.setHttpServiceRouteRadixTree(tempHttpServiceRouteRadixTree);
      gatewayDyamicConfig.setHttpServiceRouteRules(null);
    }
    //代理路由配置-填充至radixTree
    if (CollectionUtil.isNotEmpty(gatewayDyamicConfig.getProxyRouteRules())) {
      TextRadixTree<ProxyRouteRule> tempProxyRouteRadixTree = new TextRadixTree();
      gatewayDyamicConfig.getProxyRouteRules().forEach(p -> {
        tempProxyRouteRadixTree.put(p.getApiPath(), p);
      });
      gatewayDyamicConfig.setProxyRadixTree(tempProxyRouteRadixTree);
      gatewayDyamicConfig.setProxyRouteRules(null);
    }
    //限流配置初始化
    List<FlowRule> flowRules = gatewayDyamicConfig.getFlowRules();
    FlowControlManager.getInstance().initFLow(flowRules);
    this.gatewayDyamicConfig = gatewayDyamicConfig;
    return gatewayDyamicConfig;
  }
}
