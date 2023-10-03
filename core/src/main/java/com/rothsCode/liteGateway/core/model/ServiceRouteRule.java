package com.rothsCode.liteGateway.core.model;

import lombok.Data;

/**
 * @author roths
 * @Description:服务调用路由规则
 * @date 2023/8/22 20:02
 */
@Data
public class ServiceRouteRule {

  /**
   * url路由规则
   */
  private String apiPath;

  /**
   * 服务名
   */
  private String serviceName;


}
