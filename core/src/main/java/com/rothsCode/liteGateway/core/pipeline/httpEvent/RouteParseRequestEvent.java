package com.rothsCode.liteGateway.core.pipeline.httpEvent;

import static java.nio.charset.StandardCharsets.UTF_8;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.remoteConfig.GatewayDyamicConfig;
import com.rothsCode.liteGateway.core.constants.GatewayHeaderConstant;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.exception.RouteRequestException;
import com.rothsCode.liteGateway.core.model.DubboRouteRule;
import com.rothsCode.liteGateway.core.model.DubboServiceInvoker;
import com.rothsCode.liteGateway.core.model.ProxyRouteRule;
import com.rothsCode.liteGateway.core.model.ServiceRouteRule;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.RouteTypeEnum;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


/**
 * @author roths
 * @Description: 请求参数解析路由匹配事件 路由解析规则,默认http服务解析,根据请求头routeType类型来判断, 路由匹配目前只支持全匹配和后缀/**匹配,不支持/aa/**
 * 和/aa/fff 同时存在
 * @date 2023/8/13 21:42
 */
public class RouteParseRequestEvent extends HandlerEvent {

  @SneakyThrows
  @Override
  public boolean actualProcess(HandlerContext t) {
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    FullHttpRequest fullHttpRequest = (FullHttpRequest) gatewayContext.getGatewayRequest().getMsg();
    fullHttpRequest.headers().add(GatewayHeaderConstant.TRACE_ID, gatewayContext.getTraceId());
    GatewayDyamicConfig gatewayDyamicConfig = GatewayConfigLoader.getInstance()
        .getGatewayDyamicConfig();
    RouteTypeEnum routeType = RouteTypeEnum
        .getByCode(fullHttpRequest.headers().get(GatewayHeaderConstant.ROUTE_TYPE));
    switch (routeType) {
      case DUBBO:
        dubboRouteMatch(gatewayContext, fullHttpRequest, gatewayDyamicConfig);
        break;
      case URL_PROXY:
        urlProxyMatch(gatewayContext, gatewayDyamicConfig);
        break;
      default:
        httpServiceRouteMatch(gatewayContext, gatewayDyamicConfig);
    }
    return true;
  }

  private void httpServiceRouteMatch(GatewayContext gatewayContext,
      GatewayDyamicConfig gatewayDyamicConfig) throws RouteRequestException {
    //默认取路径前缀作为服务名
    if (gatewayDyamicConfig.getHttpServiceRouteRadixTree() == null) {
      String serviceName = StringUtils.substringBefore(gatewayContext.getUrlPath(), "/");
      gatewayContext.setServiceName(serviceName);
    } else {
      ServiceRouteRule serviceRouteRule = gatewayDyamicConfig.getHttpServiceRouteRadixTree()
          .findValueByKey(gatewayContext.getUrlPath());
      if (serviceRouteRule == null) {
        throw new RouteRequestException(GatewayRequestStatusEnum.NOT_MATCH_ROUTE);
      }
      gatewayContext.setServiceName(serviceRouteRule.getServiceName());
    }
    gatewayContext.setRouteType(RouteTypeEnum.HTTP_SERVICE);
  }

  private void urlProxyMatch(GatewayContext gatewayContext, GatewayDyamicConfig gatewayDyamicConfig)
      throws RouteRequestException {
    ProxyRouteRule proxyRouteRule = gatewayDyamicConfig.getProxyRadixTree()
        .findValueByKey(gatewayContext.getUrlPath());
    if (proxyRouteRule == null) {
      throw new RouteRequestException(GatewayRequestStatusEnum.NOT_MATCH_ROUTE);
    }
    gatewayContext.setRouteType(RouteTypeEnum.URL_PROXY);
    gatewayContext.setProxyRouteRule(proxyRouteRule);
  }

  private void dubboRouteMatch(GatewayContext gatewayContext, FullHttpRequest fullHttpRequest,
      GatewayDyamicConfig gatewayDyamicConfig) throws RouteRequestException {
    DubboRouteRule dubboRouteRule = gatewayDyamicConfig.getDubboRouteRadixTree()
        .findValueByKey(gatewayContext.getUrlPath());
    if (dubboRouteRule == null) {
      throw new RouteRequestException(GatewayRequestStatusEnum.NOT_MATCH_ROUTE);
    }
    gatewayContext.setRouteType(RouteTypeEnum.DUBBO);
    buildDubboInvoker(gatewayContext, fullHttpRequest, gatewayContext.getUrlPath(),
        dubboRouteRule);
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
