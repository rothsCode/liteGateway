package com.rothsCode.liteGateway.core.pipeline.loadbalance;

import java.util.HashMap;
import java.util.Map;

/**
 * @author roths
 * @Description:负载均衡工厂
 * @date 2023/8/21 11:37
 */
public class LoadBalanceFactory {

  private static final Map<String, LoadBalance> LB_MAP = new HashMap<>(4);

  static {
    LB_MAP.put(LoadBalanceStrategy.ROUND.getCode(), new RoundLB());
  }

  public static LoadBalance getLB(String type) {
    return LB_MAP.get(type);
  }
}
