package com.rothsCode.liteGateway.core.Context;

import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.model.DubboServiceInvoker;
import com.rothsCode.liteGateway.core.model.ProxyRouteRule;
import com.rothsCode.liteGateway.core.model.ServiceInfo;
import com.rothsCode.liteGateway.core.pipeline.enums.ProtocolTypeEnum;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;

/**
 * @author roths
 * @Description: 网关请求全上下文
 * @date 2023/8/14 18:28
 */
@Data
public class GatewayContext {

  /**
   * 网关链路id
   */

  private String traceId;

  /**
   * 请求体
   */
  private GatewayRequest gatewayRequest;

  /**
   * 接口相应信息
   */
  private Object response;

  /**
   * http服务调用信息
   */
  private ServiceInfo serviceInfo;

  /**
   * 代理调用信息
   */
  private ProxyRouteRule proxyRouteRule;
  /**
   * 调用路径
   */
  private String urlPath;

  private String clientIP;


  /**
   * 调用服务名
   */
  private String serviceName;

  /**
   * dubbo服务调用信息
   */
  private DubboServiceInvoker dubboServiceInvoker;

  /**
   * 调用协议
   */
  private ProtocolTypeEnum protocol = ProtocolTypeEnum.DISCOVERY;

  /**
   * 负载均衡策略
   */
  private String loadBalanceStrategy;

  /**
   * 网关异常
   */
  private Throwable throwable;

  /**
   * 网关执行状态
   */
  private GatewayContextStatusEnum status = GatewayContextStatusEnum.ENTER;

  /**
   * 网关请求状态
   */
  private GatewayRequestStatusEnum gatewayStatus = GatewayRequestStatusEnum.SUCCESS;

  /**
   * 业务请求返回状态
   */
  private int responseStatus;
  /**
   * 请求开始时间
   */
  private Long startTime;

  /**
   * 请求转发开始时间
   */
  private Long routeStartTime;

  /**
   * 请求路由返回时间
   */
  private Long routeReturnTime;

  /**
   * 请求网关结束时间
   */
  private Long endTime;

  private AtomicBoolean requestReleased = new AtomicBoolean(false);

  /**
   * 释放fullHttpRequest
   */
  public void releaseRequest() {
    if (requestReleased.compareAndSet(false, true)) {
      ReferenceCountUtil.release(gatewayRequest.getMsg());
    }
  }
}
