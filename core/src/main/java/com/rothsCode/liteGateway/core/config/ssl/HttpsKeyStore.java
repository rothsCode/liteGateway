package com.rothsCode.liteGateway.core.config.ssl;

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description:
 * @date 2023/9/1 9:53
 */
public class HttpsKeyStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpsKeyStore.class);
  private static ServerConfig serverConfig = GatewayConfigLoader.getInstance().getServerConfig();

  /**
   * 读取密钥
   *
   * @return InputStream
   * @version V1.0.0
   */
  public static InputStream getKeyStoreStream() {
    InputStream inStream = null;
    try {
      inStream = new FileInputStream(serverConfig.keystorePath);
    } catch (FileNotFoundException e) {
      LOGGER.error("读取密钥文件失败", e);
    }
    return inStream;
  }

  /**
   * 获取安全证书密码 (用于创建KeyManagerFactory)
   *
   * @return char[]
   * @version V1.0.0
   */
  public static char[] getCertificatePassword() {
    return serverConfig.certificatePassword.toCharArray();
  }

  /**
   * 获取密钥密码(证书别名密码) (用于创建KeyStore)
   *
   * @return char[]
   * @version V1.0.0
   */
  public static char[] getKeyStorePassword() {
    return serverConfig.keystorePassword.toCharArray();
  }

  /**
   * 获取证书类型
   */
  public static String getKeyStoreType() {
    return serverConfig.getKeyStoreType();
  }
}
