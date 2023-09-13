package com.rothsCode.liteGateway.core.Context;

import lombok.Getter;

/**
 * @author roths
 * @Description: 网关请求上下文流转状态
 * @date 2023/8/16 12:05
 */
@Getter
public enum GatewayContextStatusEnum {

  ENTER(1, "请求进入网关"),
  PROCESSING(2, "事件执行中"),
  WRITE_FLUSH(3, "响应写回"),
  END(4, "网关请求结束");
  private int code;
  private String desc;

  GatewayContextStatusEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

}
