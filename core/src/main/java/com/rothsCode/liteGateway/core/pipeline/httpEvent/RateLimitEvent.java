package com.rothsCode.liteGateway.core.pipeline.httpEvent;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.exception.GatewayException;
import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.model.FlowRule;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.pipeline.rateLimiter.FlowControl;
import com.rothsCode.liteGateway.core.pipeline.rateLimiter.FlowControlManager;
import com.rothsCode.liteGateway.core.pipeline.rateLimiter.RateLimitResourceTypeEnum;
import com.rothsCode.liteGateway.core.util.IPMatcher;
import com.rothsCode.liteGateway.core.util.URLUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: 限流组件
 * @date 2023/8/13 21:42
 */
public class RateLimitEvent extends HandlerEvent {

  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitEvent.class);
  @Override
  public boolean actualProcess(HandlerContext t) throws GatewayException {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    FullHttpRequest fullHttpRequest = (FullHttpRequest) gatewayContext.getGatewayRequest().getMsg();
    String path = StringUtils.substringBetween(fullHttpRequest.uri(), "/", "?");
    //针对非get方法
    if (StringUtils.isEmpty(path)) {
      path = StringUtils.substringAfter(fullHttpRequest.uri(), "/");
    }
    Assert.notEmpty(path, "路径为空");
    gatewayContext.setUrlPath(path);
    FlowControlManager flowControlManager = FlowControlManager.getInstance();
    //全局资源如果没超，则继续判断单一资源量
    boolean acquireFlag = true;
    FlowControl globalFlowControl = flowControlManager.getFLowControl(
        RateLimitResourceTypeEnum.GLOBAL.getCode());
    if (globalFlowControl != null) {
      acquireFlag = globalFlowControl.acquire();
    }
    //url patten
    if (acquireFlag) {
      //从缓存中判断是否匹配
      Boolean mathStatus = flowControlManager.getMatchStatus(gatewayContext.getUrlPath());
      if (mathStatus == null || mathStatus) {
        FlowControl pathFlowControl = flowControlManager
            .getFlowControlByRateLimitValue(gatewayContext.getUrlPath());
        if (pathFlowControl == null) {
          List<FlowRule> flowRules = flowControlManager
              .getFLowRuleByType(RateLimitResourceTypeEnum.URL.getCode());
          Boolean matchStatus = Boolean.FALSE;
          if (CollectionUtil.isNotEmpty(flowRules)) {
            for (FlowRule flowRule : flowRules) {
              if (URLUtil.matchURL(flowRule.getResourceValue(), gatewayContext.getUrlPath())) {
                matchStatus = Boolean.TRUE;
                //命中则缓存化提升性能
                flowControlManager
                    .putRateLimitValue(gatewayContext.getUrlPath(), flowRule.getResourceValue());
                pathFlowControl = flowControlManager
                    .getFLowControl(flowRule.getResourceValue());
                break;
              }
            }
          }
          flowControlManager.putRateLimitMatch(gatewayContext.getUrlPath(), matchStatus);
        }
        if (pathFlowControl != null) {
          acquireFlag = pathFlowControl.acquire();
        }
      }
    }
    //ip patten
    if (acquireFlag) {
      List<FlowRule> flowRules = flowControlManager
          .getFLowRuleByType(RateLimitResourceTypeEnum.IP.getCode());
      if (CollectionUtil.isNotEmpty(flowRules)) {
        for (FlowRule flowRule : flowRules) {
          if (IPMatcher.match(flowRule.getResourceValue(), gatewayContext.getClientIP())) {
            FlowControl flowControl = flowControlManager
                .getFLowControl(flowRule.getResourceValue());
            if (flowControl != null) {
              acquireFlag = flowControl.acquire();
            }
            break;
          }
        }
      }
    }
    //user
    if (acquireFlag) {
      String user = fullHttpRequest.headers().get(RateLimitResourceTypeEnum.USER.getCode());
      if (StringUtils.isNotBlank(user)) {
        FlowControl flowControl = flowControlManager.getFLowControl(user);
        if (flowControl != null) {
          acquireFlag = flowControl.acquire();
        }
      }
    }
    //限流处理
    if (!acquireFlag) {
      throw new GatewayException(GatewayRequestStatusEnum.RATE_LIMIT);
    }
    return true;
  }

  @Override
  public HandleEventEnum handleEvent() {
    return HandleEventEnum.RATE_LIMIT_EVENT;
  }
}
