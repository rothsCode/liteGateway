package com.rothsCode.liteGateway.core.exception;


/**
 * @author roths
 * @Description: 限流异常
 * @date 2023/8/16 11:58
 */
public class RateLimitRequestException extends GatewayException {

  public RateLimitRequestException(GatewayRequestStatusEnum status, Throwable throwable) {
    super(status, throwable);
  }

  public RateLimitRequestException(GatewayRequestStatusEnum status) {
    super(status);
  }
}
