package com.rothsCode.liteGateway.core.pipeline.httpEvent;

import cn.hutool.core.lang.Assert;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.exception.GatewayException;
import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.exception.RateLimitRequestException;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.pipeline.rateLimiter.FlowControl;
import com.rothsCode.liteGateway.core.pipeline.rateLimiter.FlowControlManager;
import com.rothsCode.liteGateway.core.util.radixTree.IPCIDRRadixTree;
import com.rothsCode.liteGateway.core.util.radixTree.TextRadixTree;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * @author roths
 * @Description: 限流组件
 * @date 2023/8/13 21:42
 */
public class RateLimitEvent extends HandlerEvent {

  @Override
  public boolean actualProcess(HandlerContext t) throws GatewayException {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    FullHttpRequest fullHttpRequest = (FullHttpRequest) gatewayContext.getGatewayRequest().getMsg();
    String path = StringUtils.substringBetween(fullHttpRequest.uri(), "/", "?");
    //针对非get方法
    if (StringUtils.isEmpty(path)) {
      path = StringUtils.substringAfter(fullHttpRequest.uri(), "/");
    }
    Assert.notEmpty(path, "path is null");
    gatewayContext.setUrlPath("/" + path);
    FlowControlManager flowControlManager = FlowControlManager.getInstance();
    //全局资源如果没超，则继续判断单一资源量
    boolean acquireFlag = true;
    FlowControl globalFlowControl = flowControlManager.getGlobalFlowControl();
    if (globalFlowControl != null) {
      acquireFlag = globalFlowControl.acquire();
    }
    //urlRateLimit
    if (acquireFlag) {
      acquireFlag = urlRateLimit(gatewayContext, flowControlManager, acquireFlag);
    }
    //ip patten
    if (acquireFlag) {
      acquireFlag = ipRateLimit(gatewayContext, flowControlManager, acquireFlag);
    }
    if (!acquireFlag) {
      throw new RateLimitRequestException(GatewayRequestStatusEnum.RATE_LIMIT);
    }
    return true;
  }

  private boolean ipRateLimit(GatewayContext gatewayContext, FlowControlManager flowControlManager,
      boolean acquireFlag) {
    IPCIDRRadixTree<FlowControl> ipRadixTree = flowControlManager.getIpcidrRadixTree();
    if (ipRadixTree != null) {
      FlowControl ipFlowControl = ipRadixTree.findValueByKey(gatewayContext.getClientIP());
      if (ipFlowControl != null) {
        acquireFlag = ipFlowControl.acquire();
      }
    }
    return acquireFlag;
  }

  private boolean urlRateLimit(GatewayContext gatewayContext, FlowControlManager flowControlManager,
      boolean acquireFlag) {
    TextRadixTree<FlowControl> urlRadixTree = flowControlManager.getUrlRadixTree();
    if (urlRadixTree != null) {
      FlowControl flowControl = urlRadixTree.findValueByKey(gatewayContext.getUrlPath());
      if (flowControl != null) {
        acquireFlag = flowControl.acquire();
      }
    }
    return acquireFlag;
  }

  @Override
  public HandleEventEnum handleEvent() {
    return HandleEventEnum.RATE_LIMIT_EVENT;
  }
}
