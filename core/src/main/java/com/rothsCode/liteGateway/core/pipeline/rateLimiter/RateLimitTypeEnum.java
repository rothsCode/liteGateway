package com.rothsCode.liteGateway.core.pipeline.rateLimiter;

/**
 * rothscode 限流资源类型
 */
public enum RateLimitTypeEnum {
  MEMORY("memory", "单机内存限流"),
  REDIS("redis", "redis分布式限流");

  private String code;
  private String desc;

  RateLimitTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (RateLimitTypeEnum e : RateLimitTypeEnum.values()) {
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
