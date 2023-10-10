package com.rothsCode.liteGateway.core.config.ssl;

import lombok.Data;

/**
 * @author roths
 * @Description:
 * @date 2023/10/8 10:52
 */
@Data
public class SSLConfig {

  // 是否开启ssl
  private boolean sslEnabled = false;
  // 是否开启双向验证
  private boolean needClientAuth = false;
  // 密匙库地址
  private String pkPath;
  // 签名证书地址
  private String caPath;
  // 证书密码
  private String passwd;

}
