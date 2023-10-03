package com.rothsCode.liteGateway.core.config.remoteConfig;

import com.rothsCode.liteGateway.core.model.DubboRouteRule;
import com.rothsCode.liteGateway.core.model.FlowRule;
import com.rothsCode.liteGateway.core.model.ProxyRouteRule;
import com.rothsCode.liteGateway.core.model.ServiceRouteRule;
import com.rothsCode.liteGateway.core.util.radixTree.TextRadixTree;
import java.util.List;
import lombok.Data;

/**
 * @author roths
 * @Description:服务端配置类
 * @date 2023/8/6 19:01
 */
@Data
public class GatewayDyamicConfig {

  /**
   * 路径白名单
   */
  private List<String> whitePathList;

  /**
   * ip白名单
   */
  private List<String> whiteIpList;

  /**
   * ip黑名单
   */
  private List<String> blackIpList;


  /**
   * dubbo路由规则
   */
  private List<DubboRouteRule> dubboRouteRules;

  /**
   * dubbo路由规则基点树
   */
  private TextRadixTree<DubboRouteRule> dubboRouteRadixTree;

  /**
   * 代理路由规则
   */
  private List<ProxyRouteRule> proxyRouteRules;

  /**
   * path Proxy SimpleRadixTree
   */
  private TextRadixTree<ProxyRouteRule> proxyRadixTree;

  /**
   * http服务发现路由规则
   */
  private List<ServiceRouteRule> httpServiceRouteRules;

  /**
   * http服务发现路由规则  SimpleRadixTree
   */
  private TextRadixTree<ServiceRouteRule> httpServiceRouteRadixTree;


  /**
   * 限流规则
   */
  private List<FlowRule> flowRules;

}
