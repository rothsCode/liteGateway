package com.rothsCode.liteGateway.core.container.Context;

import lombok.Getter;

/**
 * @author roths
 * @Description: 网关请求上下文流转状态
 * @date 2023/8/16 12:05
 */
@Getter
public enum RequestWriteStatusEnum {

  ENTER(1, "请求进入网关"),
  PROCESSING(2, "事件执行中"),
  WRITE(3, "响应写入到socket缓存"),
  FLUSH_ERROR(4, "数据发送报错"),
  FLUSH_SUCCESS(5, "数据发送成功");
  private int code;
  private String desc;

  RequestWriteStatusEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

}
