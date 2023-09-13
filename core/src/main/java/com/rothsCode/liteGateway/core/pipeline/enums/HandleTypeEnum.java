package com.rothsCode.liteGateway.core.pipeline.enums;

/**
 * rothscode 执行类型枚举
 */
public enum HandleTypeEnum {
  HTTP_REQUEST("httpRequest", "http请求执行事件"),
  HANDLE_HEAD_INDEX("handleHeadIndex", "执行起始节点位置"),
  UNKNOWN("UNKNOWN", "未知");

  private String code;
  private String desc;

  HandleTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (HandleTypeEnum e : HandleTypeEnum.values()) {
      if (e.getCode().equals(code)) {
        return e.getDesc();
      }
    }
    return UNKNOWN.getDesc();
  }

  public String getCode() {
    return this.code;
  }

  public String getDesc() {
    return this.desc;
  }
}
