package com.rothsCode.liteGateway.core.model;

import lombok.Data;

/**
 * @author roths
 * @Description:url和Proxy接口调用的映射规则
 * @date 2023/8/22 20:02
 */
@Data
public class ProxyRouteRule {

  /**
   * 接口调用路径
   */
  private String apiPath;

  /**
   * 代理hostName
   */
  private String hostName;

  /**
   * 代理端口名
   */
  private int port;


}
