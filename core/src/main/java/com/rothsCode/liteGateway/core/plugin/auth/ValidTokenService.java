package com.rothsCode.liteGateway.core.plugin.auth;

/**
 * @author roths
 * @Description: token校验器
 * @date 2023/8/22 10:33
 */
public interface ValidTokenService {


  /**
   * 校验token合法性
   *
   * @param authToken
   * @return
   */
  boolean validToken(String authToken);


}
