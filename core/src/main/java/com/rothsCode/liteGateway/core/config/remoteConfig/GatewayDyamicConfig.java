package com.rothsCode.liteGateway.core.config.remoteConfig;

import com.rothsCode.liteGateway.core.model.DubboRouteRule;
import com.rothsCode.liteGateway.core.model.FlowRule;
import com.rothsCode.liteGateway.core.model.ProxyRouteRule;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
   * 数据结构后转化的规则
   */
  private Map<String, DubboRouteRule> dubboRouteRuleMap = new ConcurrentHashMap<>();

  /**
   * 代理路由规则
   */
  private List<ProxyRouteRule> proxyRouteRules;

  /**
   * 数据结构后转化的代理规则
   */
  private Map<String, ProxyRouteRule> proxyRouteRuleMap = new ConcurrentHashMap<>();

  /**
   * 限流规则
   */
  private List<FlowRule> flowRules;
}
