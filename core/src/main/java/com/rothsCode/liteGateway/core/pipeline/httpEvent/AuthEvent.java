package com.rothsCode.liteGateway.core.pipeline.httpEvent;

import cn.hutool.core.collection.CollectionUtil;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.config.remoteConfig.GatewayDyamicConfig;
import com.rothsCode.liteGateway.core.constants.GatewayHeaderConstant;
import com.rothsCode.liteGateway.core.container.Context.GatewayContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerContext;
import com.rothsCode.liteGateway.core.pipeline.core.HandlerEvent;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import com.rothsCode.liteGateway.core.plugin.auth.ValidTokenService;
import com.rothsCode.liteGateway.core.plugin.core.PluginManager;
import com.rothsCode.liteGateway.core.util.IPMatcher;
import com.rothsCode.liteGateway.core.util.URLUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description: 鉴权事件 token校验以及黑白名单
 * @date 2023/8/21 18:00
 */
@Slf4j
public class AuthEvent extends HandlerEvent {

  private ServerConfig serverConfig;
  private ValidTokenService validTokenService;

  public AuthEvent() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    validTokenService = (ValidTokenService) PluginManager.getInstance()
        .getPluginByName(serverConfig.getAuthType());
  }


  @Override
  public boolean actualProcess(HandlerContext t) {
    if (!serverConfig.getAuthEnabled()) {
      return true;
    }
    GatewayContext gatewayContext = t.getObject(HandleParamTypeEnum.GATEWAY_CONTEXT.getCode());
    FullHttpRequest fullHttpRequest = (FullHttpRequest) gatewayContext.getGatewayRequest().getMsg();
    String requestUrl = fullHttpRequest.uri();
    GatewayDyamicConfig gatewayDyamicConfig = GatewayConfigLoader.getInstance()
        .getGatewayDyamicConfig();
    //校验ip
    if (CollectionUtil.isNotEmpty(gatewayDyamicConfig.getWhiteIpList())) {
      for (String whiteIP : gatewayDyamicConfig.getWhiteIpList()) {
        boolean whiteIPFlag = IPMatcher.match(whiteIP, gatewayContext.getClientIP());
        if (whiteIPFlag) {
          return true;
        }
      }
    }
    if (CollectionUtil.isNotEmpty(gatewayDyamicConfig.getBlackIpList())) {
      for (String blackIp : gatewayDyamicConfig.getBlackIpList()) {
        boolean blackIPFlag = IPMatcher.match(blackIp, gatewayContext.getClientIP());
        if (blackIPFlag) {
          return false;
        }
      }
    }
    //校验path
    if (CollectionUtil.isNotEmpty(gatewayDyamicConfig.getWhitePathList())) {
      for (String path : gatewayDyamicConfig.getWhitePathList()) {
        if (URLUtil.matchURL(path, requestUrl)) {
          return true;
        }
      }
    }
    String authToken = fullHttpRequest.headers().get(GatewayHeaderConstant.AUTH_TOKEN);
    //校验合法性
    return validTokenService.validToken(authToken);
  }

  @Override
  public HandleEventEnum handleEvent() {
    return HandleEventEnum.AUTH_REQUEST;
  }
}
