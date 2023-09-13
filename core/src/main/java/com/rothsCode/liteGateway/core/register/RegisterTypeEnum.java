package com.rothsCode.liteGateway.core.register;

/**
 * rothscode 注册中心类型枚举
 */
public enum RegisterTypeEnum {
  NACOS("nacos", "nacos"),
  EUREKA("eureka", "eureka"),
  UNKNOWN("UNKNOWN", "未知");

  private String code;
  private String desc;

  RegisterTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (RegisterTypeEnum e : RegisterTypeEnum.values()) {
      if (e.getCode().equals(code)) {
        return e.getDesc();
      }
    }
    return UNKNOWN.getDesc();
  }

  public static RegisterTypeEnum getByCode(String code) {
    for (RegisterTypeEnum e : RegisterTypeEnum.values()) {
      if (e.getCode().equals(code)) {
        return e;
      }
    }
    return UNKNOWN;
  }

  public String getCode() {
    return this.code;
  }

  public String getDesc() {
    return this.desc;
  }
}
