package com.rothsCode.liteGateway.core;


import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.container.ServerContainer;

/**
 * @author roths
 * @Description: 服务端启动类
 * @date 2023/8/7 22:29
 */
public class GatewayServerApplication {

  public static void main(String[] args) {
    //配置初始化
    ServerConfig serverConfig = GatewayConfigLoader.getInstance().loadConfig();
    //容器加载
    ServerContainer serverContainer = new ServerContainer(serverConfig);
    serverContainer.init();
    serverContainer.start();
    Runtime.getRuntime().addShutdownHook(new Thread(serverContainer::shutDown, "serverContainer"));
  }
}
