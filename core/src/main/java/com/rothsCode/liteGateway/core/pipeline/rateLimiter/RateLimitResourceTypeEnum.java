package com.rothsCode.liteGateway.core.pipeline.rateLimiter;

/**
 * rothscode 限流资源类型
 */
public enum RateLimitResourceTypeEnum {
  GLOBAL("global", "全局范围"),
  URL("url", "接口路径"),
  IP("ip", "访问ip"),
  USER("user", "用户");

  private String code;
  private String desc;

  RateLimitResourceTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (RateLimitResourceTypeEnum e : RateLimitResourceTypeEnum.values()) {
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
