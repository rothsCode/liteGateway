package com.rothsCode.liteGateway.core.model;

import lombok.Data;

/**
 * @author roths
 * @Description:url和dubbo接口调用的映射规则
 * @date 2023/8/22 20:02
 */
@Data
public class DubboRouteRule {

  /**
   * 接口调用路径
   */
  private String apiPath;

  /**
   * 服务名
   */
  private String serviceName;

  /**
   * 接口名
   */
  private String interfaceName;

  /**
   * 版本号
   */
  private String version;

  /**
   * 接口名
   */
  private String methodName;

  /**
   * 参数类型
   */
  private String[] paramTypes;


}
