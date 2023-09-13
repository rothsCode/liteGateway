package com.rothsCode.liteGateway.core.container;

import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.config.remoteConfig.FetchConfigFactory;
import com.rothsCode.liteGateway.core.config.remoteConfig.FetchConfigService;
import com.rothsCode.liteGateway.core.nettyServer.CustomChannel;
import com.rothsCode.liteGateway.core.nettyServer.NettyServer;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginManager;
import com.rothsCode.liteGateway.core.register.FetchRegisterService;
import com.rothsCode.liteGateway.core.register.FetchServiceFactory;
import com.rothsCode.liteGateway.core.util.ThreadFactoryImpl;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author roths
 * @Description: 服务端启动容器
 * @date 2023/8/7 22:29
 */
public class ServerContainer implements LifeCycle {

  private final ScheduledExecutorService fetchRegisterInfoExecutor =
      Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("fetchRegisterInfoThread"));
  private final ScheduledExecutorService fetchConfigInfoExecutor =
      Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("fetchConfigInfoThread"));
  private ServerConfig serverConfig;
  private NettyServer nettyServer;
  private FetchRegisterService fetchRegisterService;
  private FetchConfigService fetchConfigService;

  public ServerContainer(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }

  @Override
  public void init() {
    //server端
    nettyServer = new NettyServer(serverConfig.getNettyConfig());
    nettyServer.setCustomChannel(new CustomChannel());
    nettyServer.init();
    //拉取注册中心
    fetchRegisterService = FetchServiceFactory.create(serverConfig.getRegisterConfig());
    //异步线程加载服务信息缓存
    fetchRegisterInfoExecutor.scheduleAtFixedRate(() -> {
      fetchRegisterService.fetchRegisterService(false);
    }, 0, 5, TimeUnit.MINUTES);
    fetchConfigService = FetchConfigFactory.create(serverConfig.getConfigCenterConfig());
    //异步线程加载动态配置信息缓存
    fetchConfigInfoExecutor.scheduleAtFixedRate(() -> {
      fetchConfigService.fetchConfig();
    }, 0, 10, TimeUnit.MINUTES);

    //插件初始化
    PluginManager.getInstance().getPlugins().forEach(Plugin::init);
  }


  /**
   * 启动容器
   */
  public void start() {
    //插件启动
    PluginManager.getInstance().getPlugins().forEach(Plugin::start);
    //启动nettyServer
    nettyServer.start();

  }

  @Override
  public void shutDown() {
    fetchRegisterInfoExecutor.shutdown();
    fetchConfigInfoExecutor.shutdown();
    nettyServer.shutDown();
    //插件关闭
    PluginManager.getInstance().getPlugins().forEach(Plugin::shutDown);

  }

}
