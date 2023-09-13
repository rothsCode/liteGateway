package com.rothsCode.liteGateway.core.config.ssl;

import java.security.KeyStore;
import java.security.Security;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: SSL服务器端认证
 * @date 2023/9/1 9:52
 */
public class HttpSslContextFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpSslContextFactory.class);
  private static final String PROTOCOL = "SSLv3";//客户端可以指明为SSLv3或者TLSv1.2
  /**
   * 针对于服务器端配置
   */
  private static SSLContext sslContext = null;

  static {
    String algorithm = Security
        .getProperty("ssl.KeyManagerFactory.algorithm");
    if (algorithm == null) {
      algorithm = "SunX509";
    }
    SSLContext serverContext = null;
    try {
      KeyStore ks = KeyStore.getInstance(HttpsKeyStore.getKeyStoreType());
      ks.load(HttpsKeyStore.getKeyStoreStream(), HttpsKeyStore.getKeyStorePassword());
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
      kmf.init(ks, HttpsKeyStore.getCertificatePassword());
      serverContext = SSLContext.getInstance(PROTOCOL);
      serverContext.init(kmf.getKeyManagers(), null, null);
    } catch (Exception e) {
      LOGGER.error("初始化server SSL失败", e);
      throw new Error("Failed to initialize the server SSLContext", e);
    }
    sslContext = serverContext;
  }

  public static SSLEngine createSSLEngine() {
    SSLEngine sslEngine = sslContext.createSSLEngine();
    sslEngine.setUseClientMode(false);
    sslEngine.setNeedClientAuth(false);
    return sslEngine;
  }
}