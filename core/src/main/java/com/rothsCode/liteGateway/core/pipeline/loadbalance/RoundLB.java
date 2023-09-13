package com.rothsCode.liteGateway.core.pipeline.loadbalance;

import cn.hutool.core.collection.CollectionUtil;
import com.rothsCode.liteGateway.core.model.ServiceInfo;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author roths
 * @Description: 轮询机制
 * @date 2023/8/21 11:12
 */
public class RoundLB implements LoadBalance {

  private AtomicLong current = new AtomicLong(0);

  @Override
  public ServiceInfo select(List<ServiceInfo> serviceInfos) {
    if (CollectionUtil.isEmpty(serviceInfos)) {
      return null;
    }
    int roundIndex = (int) (current.addAndGet(1) % serviceInfos.size());
    return serviceInfos.get(roundIndex);
  }

  @Override
  public String type() {
    return LoadBalanceStrategy.ROUND.getCode();
  }
}
