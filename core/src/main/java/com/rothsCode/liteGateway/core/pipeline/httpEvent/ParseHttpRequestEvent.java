package com.rothsCode.liteGateway.core.pipeline.httpEvent;

import static java.nio.charset.StandardCharsets.UTF_8;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.remoteConfig.GatewayDyamicConfig;
import com.rothsCode.liteGateway.core.constants.GatewayHeaderConstant;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.model.DubboRouteRule;
import com.rothsCode.liteGateway.core.model.DubboServiceInvoker;
import com.rothsCode.liteGateway.core.model.ProxyRouteRule;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.ProtocolTypeEnum;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


/**
 * @author roths
 * @Description: 请求参数校验执行事件
 * @date 2023/8/13 21:42
 */
public class ParseHttpRequestEvent extends HandlerEvent {

  @Override
  public boolean actualProcess(HandlerContext t) {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    FullHttpRequest fullHttpRequest = (FullHttpRequest) gatewayContext.getGatewayRequest().getMsg();
    fullHttpRequest.headers().add(GatewayHeaderConstant.TRACE_ID, gatewayContext.getTraceId());
    //优先从dubbo路由规则中查询,匹配到则为dubbo调用，否则为http调用
    GatewayDyamicConfig gatewayDyamicConfig = GatewayConfigLoader.getInstance()
        .getGatewayDyamicConfig();
    DubboRouteRule dubboRouteRule = gatewayDyamicConfig.getDubboRouteRuleMap()
        .get(gatewayContext.getUrlPath());
    if (dubboRouteRule != null) {
      buildDubboInvoker(gatewayContext, fullHttpRequest, gatewayContext.getUrlPath(),
          dubboRouteRule);
    } else {
      //查询代理配置判断协议类型
      //查询代理映射关系
      Map<String, ProxyRouteRule> proxyRouteRuleMap = gatewayDyamicConfig.getProxyRouteRuleMap();
      if (CollectionUtil.isNotEmpty(proxyRouteRuleMap)) {
        ProxyRouteRule proxyRouteRule = proxyRouteRuleMap.get(gatewayContext.getUrlPath());
        if (proxyRouteRule != null) {
          gatewayContext.setProtocol(ProtocolTypeEnum.PROXY);
          gatewayContext.setProxyRouteRule(proxyRouteRule);
          return true;
        }
      }
      gatewayContext.setProtocol(ProtocolTypeEnum.DISCOVERY);
      //默认取路径前缀作为服务名,请求头限定以及配置优先
      String serviceName = fullHttpRequest.headers().get(GatewayHeaderConstant.SERVICE_NAME);
      //取url前缀
      if (StringUtils.isEmpty(serviceName)) {
        String url = fullHttpRequest.uri();
        serviceName = StringUtils.substringBetween(url, "/", "/");
        gatewayContext.setServiceName(serviceName);
      }
      Assert.notEmpty(serviceName, "服务名不存在");
    }

    return true;
  }

  /**
   * dubbo参数构造
   *
   * @param gatewayContext
   * @param fullHttpRequest
   * @param path
   * @param dubboRouteRule
   */
  private void buildDubboInvoker(GatewayContext gatewayContext, FullHttpRequest fullHttpRequest,
      String path, DubboRouteRule dubboRouteRule) {
    gatewayContext.setProtocol(ProtocolTypeEnum.DUBBO);
    DubboServiceInvoker dubboServiceInvoker = DubboServiceInvoker.builder()
        .interfaceName(dubboRouteRule.getInterfaceName())
        .methodName(dubboRouteRule.getMethodName())
        .paramTypes(dubboRouteRule.getParamTypes())
        .apiPath(path)
        .version(dubboRouteRule.getVersion()).build();
    gatewayContext.setServiceName(dubboRouteRule.getServiceName());
    //从url获取调用参数
    if (HttpMethod.GET.equals(fullHttpRequest.method())) {
      QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());
      List<Object> params = new ArrayList<>();
      if (CollectionUtil.isNotEmpty(queryStringDecoder.parameters())) {
        queryStringDecoder.parameters().values().forEach(p -> {
          if (p.size() > 1) {
            params.add(p);
          } else {
            params.add(p.get(0));
          }
        });
      }
      dubboServiceInvoker.setParamValues(params.toArray());
    } else {
      //从body中获取参数
      //如果参数为基本类型或者 Date,List,Map 等，则不需要转换，直接调用。
      //如果参数为其他 POJO，则使用 Map 代替。
      String bodyStr = fullHttpRequest.content().toString(UTF_8);
      String paramClass = dubboServiceInvoker.getParamTypes()[0];
      String classPrefix = StringUtils.substringBefore(paramClass, ".");
      if ("java".equals(classPrefix)) {
        //list特殊处理
        if ("java.util.List".equals(paramClass)) {
          List<Object> paramList = JSONArray.parseArray(bodyStr, Object.class);
          dubboServiceInvoker.setParamValues(new Object[]{paramList});
        } else {
          dubboServiceInvoker.setParamValues(new Object[]{bodyStr});
        }
      } else {
        Map<String, Object> paramMap = JSON.parseObject(bodyStr, HashMap.class);
        dubboServiceInvoker.setParamValues(new Object[]{paramMap});
      }
    }
    gatewayContext.setDubboServiceInvoker(dubboServiceInvoker);
  }

  @Override
  public HandleEventEnum handleEvent() {
    return HandleEventEnum.PARSE_REQUEST;
  }
}
