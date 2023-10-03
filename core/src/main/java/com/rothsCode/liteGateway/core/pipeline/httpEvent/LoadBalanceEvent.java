package com.rothsCode.liteGateway.core.pipeline.httpEvent;

import cn.hutool.core.lang.Assert;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.model.ServiceInfo;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.RouteTypeEnum;
import com.rothsCode.liteGateway.core.pipeline.loadbalance.LoadBalanceFactory;
import com.rothsCode.liteGateway.core.util.cache.ServiceCacheManger;
import java.util.List;

/**
 * @author roths
 * @Description: 负载均衡处理事件
 * @date 2023/8/13 21:42
 */

public class LoadBalanceEvent extends HandlerEvent {

  @Override
  public boolean actualProcess(HandlerContext t) {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    //服务发现走路由逻辑
    if (!RouteTypeEnum.HTTP_SERVICE.equals(gatewayContext.getRouteType())) {
      return true;
    }
    List<ServiceInfo> serviceInfoList = ServiceCacheManger.getInstance()
        .getServiceList(gatewayContext.getServiceName());
    Assert.notEmpty(serviceInfoList, "该服务不可用");
    //执行负载均衡策略
    ServiceInfo serviceInfo = LoadBalanceFactory.getLB(gatewayContext.getLoadBalanceStrategy())
        .select(serviceInfoList);
    Assert.notNull(serviceInfo, "该服务不可用");
    gatewayContext.setServiceInfo(serviceInfo);
    return true;
  }

  @Override
  public HandleEventEnum handleEvent() {
    return HandleEventEnum.LOAD_BALANCE;
  }
}
