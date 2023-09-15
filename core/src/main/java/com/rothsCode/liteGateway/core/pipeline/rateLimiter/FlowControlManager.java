package com.rothsCode.liteGateway.core.pipeline.rateLimiter;

import cn.hutool.core.collection.CollectionUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rothsCode.liteGateway.core.model.FlowRule;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author roths
 * @Description: 流控控制管理器 针对全局以及单路径资源进行流量管控
 * @date 2023/8/25 15:02
 */
public class FlowControlManager {

  private static final FlowControlManager INSTANCE = new FlowControlManager();
  /**
   * key:rateLimitValue  /xx/xx ..  Value: resourceValue
   */
  private static Cache<String, String> rateLimitCache = Caffeine.newBuilder()
      .build();
  /**
   * key:rateLimitValue  /xx/xx ..  Value: isMatch
   */
  private static Cache<String, Boolean> rateLimitMatchCache = Caffeine.newBuilder()
      .build();
  /**
   * key：resourceValue
   */
  private Map<String, FlowControl> flowControlMap = new ConcurrentHashMap();
  /**
   * key  resourceType
   */
  private Map<String, List<FlowRule>> flowTypeMap = new ConcurrentHashMap();

  public static FlowControlManager getInstance() {
    return INSTANCE;
  }

  /**
   * TODO 存在全量更新问题
   *
   * @param flowRuleList Copy on write
   */
  public synchronized void initFLow(List<FlowRule> flowRuleList) {
    if (CollectionUtil.isEmpty(flowRuleList)) {
      return;
    }
    Map<String, FlowControl> temFlowControlMap = new ConcurrentHashMap(flowControlMap.size());
    Map<String, List<FlowRule>> temFlowTypeMap = new ConcurrentHashMap(flowTypeMap.size());
    flowRuleList.forEach(f -> {
      FlowControl flowControl = new FlowControl(f.getRateLimitType(), f.getResourceValue(),
          f.getMaxPermits(), f.getWarmUpPeriodAsSecond(),
          f.getMaxWaitingRequests());
      temFlowControlMap.put(f.getResourceValue(), flowControl);
    });
    Map<String, List<FlowRule>> flowRuleMap = flowRuleList.stream()
        .collect(Collectors.groupingBy(FlowRule::getResourceType));
    temFlowTypeMap.putAll(flowRuleMap);
    flowControlMap = temFlowControlMap;
    flowTypeMap = temFlowTypeMap;
  }

  public FlowControl getFLowControl(String resourceValue) {
    return flowControlMap.get(resourceValue);
  }

  public List<FlowRule> getFLowRuleByType(String resourceType) {
    return flowTypeMap.get(resourceType);
  }

  /**
   * TODO 前缀树匹配优化 根据限流纬度业务值获取限流器 1：未匹配到规则 2：匹配到规则但是无缓存数据
   */
  public FlowControl getFlowControlByRateLimitValue(String rateLimitValue) {
    if (StringUtils.isEmpty(rateLimitValue)) {
      return null;
    }
    String resourceValue = rateLimitCache.getIfPresent(rateLimitValue);
    if (StringUtils.isNotBlank(resourceValue)) {
      return flowControlMap.get(resourceValue);
    }
    return null;
  }

  /**
   * 设置匹配映射关联数据
   */
  public void putRateLimitValue(String rateLimitValue, String resourceValue) {
    rateLimitCache.put(rateLimitValue, resourceValue);
  }

  /**
   * 设置资源是否匹配
   */
  public void putRateLimitMatch(String rateLimitValue, Boolean isMatch) {
    rateLimitMatchCache.put(rateLimitValue, isMatch);
  }

  public Boolean getMatchStatus(String rateLimitValue) {
    return rateLimitMatchCache.getIfPresent(rateLimitValue);
  }
}



