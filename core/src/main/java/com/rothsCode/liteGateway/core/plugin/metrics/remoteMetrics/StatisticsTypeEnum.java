package com.rothsCode.liteGateway.core.plugin.metrics.remoteMetrics;

/**
 * rothscode 协议类型
 */
public enum StatisticsTypeEnum {
  TOTAL("count", "total", "总调用次数"),
  SUCCESS("count", "success", "成功调用次数"),
  ERROR("count", "error", "异常调用次数"),
  API_TIME("count", "apiTime", "接口响应时间"),
  API_TOTAL_TIME("count", "apiTotalTime", "接口总响应时间"),
  GATEWAY_TIME("count", "gatewayTime", "网关处理时间"),
  GATEWAY_TOTAL_TIME("count", "gatewayTotalTime", "网关处理总时间"),
  TIMEOUT("count", "timeOut", "超时调用次数"),
  MAX_TIME("max", "maxTime", "调用峰值时间"),
  READ("count", "read", "读次数"),
  WRITE("count", "write", "写次数"),
  ;
  private String type;
  private String code;
  private String desc;

  StatisticsTypeEnum(String type, String code, String desc) {
    this.type = type;
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (StatisticsTypeEnum e : StatisticsTypeEnum.values()) {
      if (e.getCode().equals(code)) {
        return e.getDesc();
      }
    }
    return null;
  }

  public boolean isCounter() {
    return "count".equals(type);
  }

  public boolean isMaxUpdater() {
    return "max".equals(type);
  }

  public String getCode() {
    return this.code;
  }

  public String getDesc() {
    return this.desc;
  }
}
