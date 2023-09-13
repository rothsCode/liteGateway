//package com.rothsCode.liteGateway.core.plugin.auth;
//
//
//import com.rothsCode.liteGateway.core.plugin.core.Plugin;
//import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
//
///**
// * @author roths
// * @Description: token默认处理器
// * @date 2023/8/22 10:35
// */
//public class JWTValidTokenService implements Plugin, ValidTokenService {
//
//  @Override
//  public boolean validToken(String authToken) {
//    return true;
//  }
//
//  @Override
//  public boolean checkInit() {
//    return true;
//  }
//
//  @Override
//  public String pluginName() {
//    return PluginEnum.JWT_AUTH.getCode();
//  }
//
//  @Override
//  public void init() {
//
//  }
//
//  @Override
//  public void start() {
//
//  }
//
//  @Override
//  public void shutDown() {
//
//  }
//}
