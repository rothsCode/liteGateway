package com.rothsCode.liteGateway.core.pipeline.rateLimiter;

import cn.hutool.core.collection.CollectionUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rothsCode.liteGateway.core.model.FlowRule;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author roths
 * @Description: 流控控制管理器 针对全局以及单路径资源进行流量管控
 * @date 2023/8/25 15:02
 */
public class FlowControlManager {

  private static final FlowControlManager INSTANCE = new FlowControlManager();
  /**
   * key：path
   */
  private Cache<String, FlowControl> FLOW_CONTROL_CACHE = Caffeine.newBuilder()
      .build();
  /**
   * key  resourceValue
   */
  private Cache<String, List<FlowRule>> FLOW_CONTROL_TYPE_CACHE = Caffeine.newBuilder()
      .build();

  public static FlowControlManager getInstance() {
    return INSTANCE;
  }

  public void initFLow(List<FlowRule> flowRuleList) {
    if (CollectionUtil.isEmpty(flowRuleList)) {
      return;
    }
    flowRuleList.forEach(f -> {
      FlowControl flowControl = new FlowControl(f.getRateLimitType(), f.getResourceValue(),
          f.getMaxPermits(), f.getWarmUpPeriodAsSecond(),
          f.getMaxWaitingRequests());
      FLOW_CONTROL_CACHE.put(f.getResourceValue(), flowControl);
    });
    Map<String, List<FlowRule>> flowRuleMap = flowRuleList.stream()
        .collect(Collectors.groupingBy(FlowRule::getResourceType));
    FLOW_CONTROL_TYPE_CACHE.putAll(flowRuleMap);
  }

  public FlowControl getFLowControl(String resourceValue) {
    return FLOW_CONTROL_CACHE.getIfPresent(resourceValue);
  }

  public List<FlowRule> getFLowRuleByType(String resourceType) {
    return FLOW_CONTROL_TYPE_CACHE.getIfPresent(resourceType);
  }
}



