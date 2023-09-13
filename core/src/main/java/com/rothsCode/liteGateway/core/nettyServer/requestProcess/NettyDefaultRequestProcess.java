package com.rothsCode.liteGateway.core.nettyServer.requestProcess;

import com.rothsCode.liteGateway.core.Context.GatewayContext;
import com.rothsCode.liteGateway.core.Context.GatewayContextStatusEnum;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerChainEngineFactory;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleTypeEnum;
import com.rothsCode.liteGateway.core.util.MdcUtil;
import com.rothsCode.liteGateway.core.util.SnowFlake;


/**
 * @author roths
 * @Description: 默认同步请求处理器
 * @date 2023/8/13 11:27
 */
public class NettyDefaultRequestProcess implements NettyRequestProcess {

  @Override
  public void processRequest(GatewayContext gatewayContext) {
    gatewayContext.setStatus(GatewayContextStatusEnum.PROCESSING);
    String traceId = SnowFlake.generateIdStr();
    MdcUtil.setTraceId(traceId);
    gatewayContext.setTraceId(traceId);
    HandlerContext handlerContext = new HandlerContext();
    handlerContext.put(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode(), gatewayContext);
    HandlerChainEngineFactory.getHandlerChainEngine(HandleTypeEnum.HTTP_REQUEST.getCode())
        .processEvent(handlerContext);
  }
}
