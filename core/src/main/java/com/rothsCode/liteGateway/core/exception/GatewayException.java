package com.rothsCode.liteGateway.core.exception;


/**
 * @author roths
 * @Description: 异常分为网关内部异常以及服务下游异常
 * @date 2023/8/16 11:58
 */
public class GatewayException extends Exception {

  public GatewayException(GatewayRequestStatusEnum status, Throwable throwable) {
    super(status.getDesc(), throwable);
  }

  public GatewayException(GatewayRequestStatusEnum status) {
    super(status.getDesc());
  }

}
