package com.rothsCode.liteGateway.core.pipeline.loadbalance;

import cn.hutool.core.collection.CollectionUtil;
import com.rothsCode.liteGateway.core.model.ServiceInfo;
import java.util.List;
import java.util.Random;

/**
 * @author roths
 * @Description: 随机机制
 * @date 2023/8/21 11:12
 */
public class RandomLB implements LoadBalance {

  @Override
  public ServiceInfo select(List<ServiceInfo> serviceInfos) {
    if (CollectionUtil.isEmpty(serviceInfos)) {
      return null;
    }
    Random random = new Random();
    int roundIndex = random.nextInt(serviceInfos.size());
    return serviceInfos.get(roundIndex);
  }

  @Override
  public String type() {
    return LoadBalanceStrategy.RANDOM.getCode();
  }
}
