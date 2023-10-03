package com.rothsCode.liteGateway.core.pipeline.enums;

/**
 * rothscode 事件类型枚举
 */
public enum HandleEventEnum {
  RATE_LIMIT_EVENT("rateLimit", 1, HandleEventTypeEnum.PRE.getCode(), "请求限流事件"),
  PARSE_REQUEST("parseRequest", 2, HandleEventTypeEnum.PRE.getCode(), "解析请求参数事件"),
  VALID_REQUEST("validRequest", 3, HandleEventTypeEnum.PRE.getCode(), "请求参数校验事件"),
  AUTH_REQUEST("authRequest", 4, HandleEventTypeEnum.PRE.getCode(), "鉴权请求事件"),
  LOAD_BALANCE("loadBalance", 5, HandleEventTypeEnum.PRE.getCode(), "负载均衡事件"),
  DISCOVERY_ROUTE_EVENT("discoveryRoute", 6, HandleEventTypeEnum.MID.getCode(), "服务发现调用事件"),
  DUBBO_ROUTE_EVENT("dubboRoute", 7, HandleEventTypeEnum.MID.getCode(), "dubbo调用事件"),
  PROXY_ROUTE_EVENT("proxyRoute", 8, HandleEventTypeEnum.MID.getCode(), "请求代理调用事件"),
  LOG_REQUEST_EVENT("logRequest", 10, HandleEventTypeEnum.POST.getCode(), "日志采集事件"),
  METRICS_REQUEST("metricsRequest", 11, HandleEventTypeEnum.POST.getCode(), "请求统计分析事件");

  private String code;
  private int sort;
  /**
   * 事件类型
   */
  private String eventType;
  private String desc;

  HandleEventEnum(String code, int sort, String eventType, String desc) {
    this.code = code;
    this.desc = desc;
    this.sort = sort;
    this.eventType = eventType;
  }

  public static String getNameByCode(String code) {
    for (HandleEventEnum e : HandleEventEnum.values()) {
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

  public int getSort() {
    return this.sort;
  }

  public String getEventType() {
    return this.eventType;
  }
}
