package com.rothsCode.liteGateway.core.plugin.httpClient;

import java.util.concurrent.CompletableFuture;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

/**
 * @author roths
 * @Description:
 * @date 2023/9/12 10:51
 */
public interface IAsyncHttpClient {

  /**
   * 异步调用
   *
   * @param request
   * @return
   */
  CompletableFuture<Response> executeRequest(Request request);

  /**
   * 异步调用带回调函数
   *
   * @param request
   * @param handler
   * @param <T>
   * @return
   */
  <T> CompletableFuture<T> executeRequest(Request request, AsyncHandler<T> handler);
}
