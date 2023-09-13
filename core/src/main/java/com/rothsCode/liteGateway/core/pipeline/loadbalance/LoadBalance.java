package com.rothsCode.liteGateway.core.pipeline.loadbalance;

import com.rothsCode.liteGateway.core.model.ServiceInfo;
import java.util.List;

/**
 * @author roths
 * @Description:
 * @date 2023/8/18 16:21
 */
public interface LoadBalance {


  ServiceInfo select(List<ServiceInfo> serviceInfos);


  String type();

}
