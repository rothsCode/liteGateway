package com.rothsCode.liteGateway.core.pipeline.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * rothscode 路由匹配类型
 */
public enum RouteTypeEnum {
  DUBBO("dubbo", "dubbo调用"),
  HTTP_SERVICE("httpService", "http服务调用调用"),
  URL_PROXY("urlProxy", "url代理调用");

  private String code;
  private String desc;

  RouteTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static RouteTypeEnum getByCode(String code) {
    if (StringUtils.isEmpty(code)) {
      return HTTP_SERVICE;
    }
    for (RouteTypeEnum e : RouteTypeEnum.values()) {
      if (e.getCode().equals(code)) {
        return e;
      }
    }
    return HTTP_SERVICE;
  }

  public String getCode() {
    return this.code;
  }

  public String getDesc() {
    return this.desc;
  }
}
