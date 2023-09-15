package com.rothsCode.liteGateway.core.nettyServer.requestProcess;

import com.rothsCode.liteGateway.core.container.Context.GatewayContext;

/**
 * @author roths
 * @Description:
 * @date 2023/9/7 10:11
 */
public interface NettyRequestProcess {


  /**
   * 处理请求
   *
   * @param gatewayContext
   */
  void processRequest(GatewayContext gatewayContext);

}
