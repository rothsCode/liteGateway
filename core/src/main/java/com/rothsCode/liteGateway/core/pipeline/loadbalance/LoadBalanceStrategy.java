package com.rothsCode.liteGateway.core.pipeline.loadbalance;

/**
 * rothscode 执行类型枚举
 */
public enum LoadBalanceStrategy {
  ROUND("round", "轮询"),
  RANDOM("random", "随机"),
  WEIGHT("weight", "权重"),
  IP_HASH("ipHash", "ip绑定");
  private String code;
  private String desc;

  LoadBalanceStrategy(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (LoadBalanceStrategy e : LoadBalanceStrategy.values()) {
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
