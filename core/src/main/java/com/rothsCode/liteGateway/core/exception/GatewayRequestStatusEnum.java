package com.rothsCode.liteGateway.core.exception;

/**
 * @author roths
 * @Description: 网关请求状态码
 * @date 2023/8/16 12:05
 */

public enum GatewayRequestStatusEnum {

  SUCCESS(200, "请求成功"),
  NOT_MATCH_ROUTE(530, "没有对应的路由规则请检查"),
  INTERNAL_ERROR(540, "网关内部异常"),
  TIMEOUT(550, "网关请求超时"),
  DUBBO_ERROR(560, "dubbo調用异常"),
  RATE_LIMIT(570, "当前访问量过大！请稍后再试"),
  SERVICE_ERROR(580, "服务調用异常"),
  REQUEST_PROCESS_ERROR(590, "请求处理异常"),
  ES_INDEX_ERROR(591, "es索引异常")
  ;

  private int code;
  private String desc;

  GatewayRequestStatusEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(int code) {
    for (GatewayRequestStatusEnum e : GatewayRequestStatusEnum.values()) {
      if (e.getCode() == code) {
        return e.getDesc();
      }
    }
    return null;
  }

  public int getCode() {
    return this.code;
  }

  public String getDesc() {
    return this.desc;
  }

}
