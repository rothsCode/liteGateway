package com.rothsCode.liteGateway.core.plugin.httpClient;

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import io.netty.buffer.PooledByteBufAllocator;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: 异步http调用客户端插件
 * @date 2023/9/12 10:36
 */
public class AsyncNettyHttpClient implements IAsyncHttpClient, Plugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncNettyHttpClient.class);

  private final AtomicBoolean startStatus = new AtomicBoolean(false);

  private ServerConfig serverConfig;

  private AsyncHttpClient asyncHttpClient;

  private DefaultAsyncHttpClientConfig.Builder clientBuilder;

  @Override
  public boolean checkInit() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    return PluginEnum.ASYNC_NETTY_HTTP_CLIENT.getCode().equals(serverConfig.getHttpClientType());
  }

  @Override
  public String pluginName() {
    return PluginEnum.ASYNC_NETTY_HTTP_CLIENT.getCode();
  }

  @Override
  public void init() {
    this.clientBuilder = new DefaultAsyncHttpClientConfig.Builder()
        .setFollowRedirect(false)
        .setEventLoopGroup(serverConfig.getNettyConfig().getWorkGroup())
        .setConnectTimeout(serverConfig.getHttpConnectTimeout())
        .setRequestTimeout(serverConfig.getHttpRequestTimeout())
        .setMaxRequestRetry(serverConfig.getHttpMaxRequestRetry())
        .setAllocator(PooledByteBufAllocator.DEFAULT)
        .setCompressionEnforced(true)
        .setMaxConnections(serverConfig.getHttpMaxConnections())
        .setMaxConnectionsPerHost(serverConfig.getHttpConnectionsPerHost())
        .setPooledConnectionIdleTimeout(serverConfig.getHttpPooledConnectionIdleTimeout());
  }

  @Override
  public void start() {
    if (!startStatus.compareAndSet(false, true)) {
      return;
    }
    this.asyncHttpClient = new DefaultAsyncHttpClient(clientBuilder.build());
  }

  @Override
  public void shutDown() {
    if (asyncHttpClient != null) {
      try {
        this.asyncHttpClient.close();
      } catch (IOException e) {
        LOGGER.error("AsyncNettyHttpClient.shutdown error:{}", e);
      }
    }
  }

  @Override
  public CompletableFuture<Response> executeRequest(Request request) {
    ListenableFuture<Response> future = asyncHttpClient.executeRequest(request);
    return future.toCompletableFuture();
  }

  @Override
  public <T> CompletableFuture<T> executeRequest(Request request, AsyncHandler<T> handler) {
    ListenableFuture<T> future = asyncHttpClient.executeRequest(request, handler);
    return future.toCompletableFuture();
  }
}
