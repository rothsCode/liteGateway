package com.rothsCode.liteGateway.core.pipeline.enums;

/**
 * rothscode 协议类型
 */
public enum ProtocolTypeEnum {
  DISCOVERY("discovery", "服务发现调用"),
  DUBBO("dubbo", "dubbo调用"),
  PROXY("proxy", "代理调用"),
  UNKNOWN("UNKNOWN", "未知");

  private String code;
  private String desc;

  ProtocolTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (ProtocolTypeEnum e : ProtocolTypeEnum.values()) {
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
