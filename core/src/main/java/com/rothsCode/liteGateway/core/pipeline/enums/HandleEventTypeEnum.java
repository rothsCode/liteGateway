package com.rothsCode.liteGateway.core.pipeline.enums;

/**
 * rothscode 执行类型枚举
 */
public enum HandleEventTypeEnum {
  PRE("pre", "前置事件"),
  MID("mid", "中置事件"),
  POST("post", "后置事件"),
  ALL("all", "所有事件链");

  private String code;
  private String desc;

  HandleEventTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (HandleEventTypeEnum e : HandleEventTypeEnum.values()) {
      if (e.getCode().equals(code)) {
        return e.getDesc();
      }
    }
    return null;
  }

  public String getCode() {
    return this.code;
  }

  public String getDesc() {
    return this.desc;
  }
}
