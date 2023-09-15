package com.rothsCode.liteGateway.core.container.Context;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * @author roths
 * @Description: 网关请求
 * @date 2023/8/13 16:34
 */
@Data
public class GatewayRequest {

  /**
   * channel上下文
   */
  private ChannelHandlerContext ctx;

  /**
   * 请求业务信息
   */
  private Object msg;


}
