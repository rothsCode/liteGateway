package com.rothsCode.liteGateway.core.exception;


/**
 * @author roths
 * @Description: es相关异常
 * @date 2023/8/16 11:58
 */
public class EsException extends GatewayException {

  public EsException(GatewayRequestStatusEnum status, Throwable throwable) {
    super(status, throwable);
  }

  public EsException(GatewayRequestStatusEnum status) {
    super(status);
  }
}
