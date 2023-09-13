package com.rothsCode.liteGateway.core.config.remoteConfig;

/**
 * rothscode 注册中心类型枚举
 */
public enum ConfigTypeEnum {
  NACOS("nacos", "nacos"),
  APOLLO("apollo", "apollo"),
  UNKNOWN("UNKNOWN", "未知");

  private String code;
  private String desc;

  ConfigTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (ConfigTypeEnum e : ConfigTypeEnum
        .values()) {
      if (e.getCode().equals(code)) {
        return e.getDesc();
      }
    }
    return UNKNOWN.getDesc();
  }

  public static ConfigTypeEnum getByCode(String code) {
    for (ConfigTypeEnum e : ConfigTypeEnum
        .values()) {
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
