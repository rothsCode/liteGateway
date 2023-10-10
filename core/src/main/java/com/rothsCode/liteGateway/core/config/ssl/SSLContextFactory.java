package com.rothsCode.liteGateway.core.config.ssl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * @author roths
 * @Description: ssl
 * @date 2023/10/8 10:34
 */
@Slf4j
public class SSLContextFactory {

  private static final String PROTOCOL = "TLS";

  private static final String KEY_STORE_TYPE = "JKS";

  private static final String CLASS_PATH_PREFIX = "classpath:";

  private static final String JDK__ALGORITHM = "SunX509";

  private static final String[] ENABLE_PROTOCOL = {"TLSv1", "TLSv1.1",
      "TLSv1.2"};

  private static SSLContext SERVER_CONTEXT;// 服务器安全套接字协议

  private static SslContext openSslContext;

  private static SSLContext CLIENT_CONTEXT;// 客户端安全套接字协议

  private static SslContext openSslClientContext;

  /**
   * 单向验证jdkSsl
   *
   * @param pkPath
   * @param passwd
   * @return
   */
  public static SSLContext getServerContext(String pkPath, String passwd) {
    if (SERVER_CONTEXT != null) {
      return SERVER_CONTEXT;
    }
    InputStream in = null;

    try {
      // 密钥管理器
      KeyManagerFactory kmf = null;
      if (pkPath != null) {
        // 密钥库KeyStore
        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (pkPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          in = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(pkPath, CLASS_PATH_PREFIX));
        } else {
          in = new FileInputStream(pkPath);
        }
        // 加载服务端的KeyStore ；sNetty是生成仓库时设置的密码，用于检查密钥库完整性的密码
        ks.load(in, passwd.toCharArray());

        kmf = KeyManagerFactory.getInstance(JDK__ALGORITHM);
        // 初始化密钥管理器
        kmf.init(ks, passwd.toCharArray());
      }
      // 获取安全套接字协议（TLS协议）的对象
      SERVER_CONTEXT = SSLContext.getInstance(PROTOCOL);
      // 初始化此上下文
      // 参数一：认证的密钥 参数二：对等信任认证 参数三：伪随机数生成器 。 由于单向认证，服务端不用验证客户端，所以第二个参数为null
      SERVER_CONTEXT.init(kmf.getKeyManagers(), null, null);

    } catch (Exception e) {
      throw new Error("Failed to initialize the server-side SSLContext",
          e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    }
    return SERVER_CONTEXT;
  }

  /**
   * 单向验证 openSsl
   *
   * @param pkPath
   * @param passwd
   * @return
   */
  public static SslContext getOpenSslServerContext(String pkPath,
      String passwd) {
    if (openSslContext != null) {
      return openSslContext;
    }
    InputStream in = null;
    try {
      // 密钥管理器
      KeyManagerFactory kmf = null;
      if (pkPath != null) {
        // 密钥库KeyStore
        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (pkPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          in = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(pkPath, CLASS_PATH_PREFIX));
        } else {
          in = new FileInputStream(pkPath);
        }
        // 加载服务端的KeyStore ；sNetty是生成仓库时设置的密码，用于检查密钥库完整性的密码
        ks.load(in, passwd.toCharArray());

        kmf = KeyManagerFactory.getInstance(JDK__ALGORITHM);
        // 初始化密钥管理器
        kmf.init(ks, passwd.toCharArray());
      }
      openSslContext = SslContextBuilder.forServer(kmf)
          .sslProvider(SslProvider.OPENSSL).build();
      return openSslContext;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    }

    return null;

  }

  public static SSLContext getClientContext(String pkPath, String passwd) {
    if (CLIENT_CONTEXT != null) {
      return CLIENT_CONTEXT;
    }

    InputStream tIN = null;
    try {
      // 信任库
      TrustManagerFactory tf = null;
      if (pkPath != null) {
        // 密钥库KeyStore
        KeyStore tks = KeyStore.getInstance(KEY_STORE_TYPE);
        // 加载客户端证书
        if (pkPath.startsWith(CLASS_PATH_PREFIX)) {
          tIN = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(pkPath, CLASS_PATH_PREFIX));
        } else {
          tIN = new FileInputStream(pkPath);
        }
        tks.load(tIN, passwd.toCharArray());
        tf = TrustManagerFactory.getInstance(JDK__ALGORITHM);
        // 初始化信任库
        tf.init(tks);
      }

      CLIENT_CONTEXT = SSLContext.getInstance(PROTOCOL);
      // 设置信任证书
      CLIENT_CONTEXT.init(null,
          tf == null ? null : tf.getTrustManagers(), null);

    } catch (Exception e) {
      throw new Error("Failed to initialize the client-side SSLContext");
    } finally {
      if (tIN != null) {
        try {
          tIN.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return CLIENT_CONTEXT;
  }

  public static SslContext getOpenSslClientContext(String pkPath,
      String passwd) {

    if (openSslClientContext != null) {
      return openSslClientContext;
    }

    InputStream tIN = null;
    try {

      // 信任库
      TrustManagerFactory tf = null;
      if (pkPath != null) {
        // 密钥库KeyStore
        KeyStore tks = KeyStore.getInstance(KEY_STORE_TYPE);
        // 加载客户端证书
        if (pkPath.startsWith(CLASS_PATH_PREFIX)) {
          tIN = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(pkPath, CLASS_PATH_PREFIX));
        } else {
          tIN = new FileInputStream(pkPath);
        }
        tks.load(tIN, passwd.toCharArray());
        tf = TrustManagerFactory.getInstance(JDK__ALGORITHM);
        // 初始化信任库
        tf.init(tks);
      }

      openSslClientContext = SslContextBuilder.forClient()
          .sslProvider(SslProvider.OPENSSL).trustManager(tf).build();

      return openSslClientContext;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (tIN != null) {
        try {
          tIN.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    }

    return null;

  }

  /**
   * 双向验证ssl
   *
   * @param pkPath
   * @param caPath
   * @param passwd
   * @return
   * @see
   */
  public static SSLContext getServerContext(String pkPath, String caPath,
      String passwd) {
    if (SERVER_CONTEXT != null) {
      return SERVER_CONTEXT;
    }
    InputStream in = null;
    InputStream tIN = null;

    try {
      // 密钥管理器
      KeyManagerFactory kmf = null;
      if (pkPath != null) {
        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (pkPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          in = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(pkPath, CLASS_PATH_PREFIX));
        } else {
          in = new FileInputStream(pkPath);
        }
        ks.load(in, passwd.toCharArray());

        kmf = KeyManagerFactory.getInstance(JDK__ALGORITHM);
        kmf.init(ks, passwd.toCharArray());
      }
      // 信任库
      TrustManagerFactory tf = null;
      if (caPath != null) {
        KeyStore tks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (caPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          tIN = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(caPath, CLASS_PATH_PREFIX));
        } else {
          tIN = new FileInputStream(caPath);
        }
        tks.load(tIN, passwd.toCharArray());
        tf = TrustManagerFactory.getInstance(JDK__ALGORITHM);
        tf.init(tks);
      }

      SERVER_CONTEXT = SSLContext.getInstance(PROTOCOL);

      // 初始化此上下文
      // 参数一：认证的密钥 参数二：对等信任认证 参数三：伪随机数生成器 。 由于单向认证，服务端不用验证客户端，所以第二个参数为null
      // 单向认证？无需验证客户端证书
      if (tf == null) {
        SERVER_CONTEXT.init(kmf.getKeyManagers(), null, null);
      }
      // 双向认证，需要验证客户端证书
      else {
        SERVER_CONTEXT.init(kmf.getKeyManagers(),
            tf.getTrustManagers(), null);
      }

    } catch (Exception e) {
      throw new Error("Failed to initialize the server-side SSLContext",
          e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (tIN != null) {
        try {
          tIN.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return SERVER_CONTEXT;
  }

  public static SslContext getOpenSslServerContext(String pkPath,
      String caPath, String passwd) {
    if (openSslContext != null) {
      return openSslContext;
    }

    InputStream in = null;
    InputStream tIN = null;
    try {

      // 密钥管理器
      KeyManagerFactory kmf = null;
      if (pkPath != null) {
        // 密钥库KeyStore
        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (pkPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          in = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(pkPath, CLASS_PATH_PREFIX));
        } else {
          in = new FileInputStream(pkPath);
        }
        // 加载服务端的KeyStore ；sNetty是生成仓库时设置的密码，用于检查密钥库完整性的密码
        ks.load(in, passwd.toCharArray());

        kmf = KeyManagerFactory.getInstance(JDK__ALGORITHM);
        // 初始化密钥管理器
        kmf.init(ks, passwd.toCharArray());
      }
      // 信任库
      TrustManagerFactory tf = null;
      if (caPath != null) {
        KeyStore tks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (caPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          tIN = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(caPath, CLASS_PATH_PREFIX));
        } else {
          tIN = new FileInputStream(caPath);
        }
        tks.load(tIN, passwd.toCharArray());
        tf = TrustManagerFactory.getInstance(JDK__ALGORITHM);
        tf.init(tks);
      }

      openSslContext = SslContextBuilder.forServer(kmf).trustManager(tf)
          .sslProvider(SslProvider.OPENSSL).build();
      return openSslContext;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (tIN != null) {
        try {
          tIN.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    }

    return null;

  }

  public static SSLContext getClientContext(String pkPath, String caPath,
      String passwd) {
    if (CLIENT_CONTEXT != null) {
      return CLIENT_CONTEXT;
    }

    InputStream in = null;
    InputStream tIN = null;
    try {
      KeyManagerFactory kmf = null;
      if (pkPath != null) {
        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (pkPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          in = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(pkPath, CLASS_PATH_PREFIX));
        } else {
          in = new FileInputStream(pkPath);
        }
        ks.load(in, passwd.toCharArray());
        kmf = KeyManagerFactory.getInstance(JDK__ALGORITHM);
        kmf.init(ks, passwd.toCharArray());
      }

      TrustManagerFactory tf = null;
      if (caPath != null) {
        KeyStore tks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (caPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          tIN = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(caPath, CLASS_PATH_PREFIX));
        } else {
          tIN = new FileInputStream(caPath);
        }
        tks.load(tIN, passwd.toCharArray());
        tf = TrustManagerFactory.getInstance(JDK__ALGORITHM);
        tf.init(tks);
      }

      CLIENT_CONTEXT = SSLContext.getInstance(PROTOCOL);
      // 初始化此上下文
      // 参数一：认证的密钥 参数二：对等信任认证 参数三：伪随机数生成器 。 由于单向认证，服务端不用验证客户端，所以第二个参数为null
      CLIENT_CONTEXT.init(kmf.getKeyManagers(), tf.getTrustManagers(),
          null);

    } catch (Exception e) {
      throw new Error("Failed to initialize the client-side SSLContext");
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
      if (tIN != null) {
        try {
          tIN.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    }

    return CLIENT_CONTEXT;
  }

  public static SslContext getOpenSslClientContext(String pkPath,
      String caPath, String passwd) {

    if (openSslClientContext != null) {
      return openSslClientContext;
    }

    InputStream in = null;
    InputStream tIN = null;
    try {

      // 密钥管理器
      KeyManagerFactory kmf = null;
      if (pkPath != null) {
        // 密钥库KeyStore
        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (pkPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          in = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(pkPath, CLASS_PATH_PREFIX));
        } else {
          in = new FileInputStream(pkPath);
        }
        // 加载服务端的KeyStore ；sNetty是生成仓库时设置的密码，用于检查密钥库完整性的密码
        ks.load(in, passwd.toCharArray());

        kmf = KeyManagerFactory.getInstance(JDK__ALGORITHM);
        // 初始化密钥管理器
        kmf.init(ks, passwd.toCharArray());
      }

      // 信任库
      TrustManagerFactory tf = null;
      if (caPath != null) {
        KeyStore tks = KeyStore.getInstance(KEY_STORE_TYPE);
        if (caPath.startsWith(CLASS_PATH_PREFIX)) {
          // 加载服务端证书
          tIN = SSLContextFactory.class
              .getResourceAsStream(StringUtils.substringAfter(caPath, CLASS_PATH_PREFIX));
        } else {
          tIN = new FileInputStream(caPath);
        }
        tks.load(tIN, passwd.toCharArray());
        tf = TrustManagerFactory.getInstance(JDK__ALGORITHM);
        tf.init(tks);
      }

      openSslClientContext = SslContextBuilder.forClient()
          .sslProvider(SslProvider.OPENSSL).keyManager(kmf)
          .trustManager(tf).build();

      return openSslClientContext;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
      if (tIN != null) {
        try {
          tIN.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    }

    return null;

  }

  /**
   * Description:
   *
   * @return
   * @see
   */
  public static SSLEngine getSslServerEngine(SSLConfig sslConfig) {

    SSLEngine sslEngine = null;
    if (sslConfig.isNeedClientAuth()) {
      sslEngine = getServerContext(sslConfig.getPkPath(),
          sslConfig.getCaPath(), sslConfig.getPasswd())
          .createSSLEngine();
    } else {
      sslEngine = getServerContext(sslConfig.getPkPath(),
          sslConfig.getPasswd()).createSSLEngine();
    }
    sslEngine.setUseClientMode(false);
    sslEngine.setEnabledProtocols(ENABLE_PROTOCOL);
    // false为单向认证，true为双向认证
    sslEngine.setNeedClientAuth(sslConfig.isNeedClientAuth());
    return sslEngine;
  }

  public static SSLEngine getOpenSslServerEngine(SSLConfig sslConfig,
      ByteBufAllocator alloc) {
    SSLEngine sslEngine = null;
    if (sslConfig.isNeedClientAuth()) {
      sslEngine = getOpenSslServerContext(sslConfig.getPkPath(),
          sslConfig.getCaPath(), sslConfig.getPasswd()).newEngine(
          alloc);
    } else {
      sslEngine = getOpenSslServerContext(sslConfig.getPkPath(),
          sslConfig.getPasswd()).newEngine(alloc);
    }

    sslEngine.setUseClientMode(false);
    sslEngine.setEnabledProtocols(ENABLE_PROTOCOL);
    // false为单向认证，true为双向认证
    sslEngine.setNeedClientAuth(sslConfig.isNeedClientAuth());
    return sslEngine;
  }

  public static SSLEngine getSslClientEngine(String pkPath, String caPath,
      String passwd, boolean isNeedClientAuth) {

    SSLEngine sslEngine = null;
    if (isNeedClientAuth) {
      sslEngine = getClientContext(pkPath, caPath, passwd)
          .createSSLEngine();
    } else {
      sslEngine = getClientContext(pkPath, passwd).createSSLEngine();

    }
    sslEngine.setEnabledProtocols(ENABLE_PROTOCOL);
    sslEngine.setUseClientMode(true);
    return sslEngine;
  }

  public static SSLEngine getOpenSslClientEngine(String pkPath,
      String caPath, String passwd, ByteBufAllocator alloc,
      boolean isNeedClientAuth) {

    SSLEngine sslEngine = null;
    if (isNeedClientAuth) {
      sslEngine = getOpenSslClientContext(pkPath, caPath, passwd)
          .newEngine(alloc);
    } else {
      sslEngine = getOpenSslClientContext(pkPath, passwd)
          .newEngine(alloc);
    }
    sslEngine.setEnabledProtocols(ENABLE_PROTOCOL);
    sslEngine.setUseClientMode(true);
    return sslEngine;
  }

  /**
   * Description:
   *
   * @return
   * @see
   */
  public static SslHandler getSslHandler(SSLConfig sslConfig) {

    if (sslConfig != null && sslConfig.isSslEnabled()) {
      return new SslHandler(getSslServerEngine(sslConfig));
    } else {
      return null;
    }
  }

  public static SslHandler getOpenSslHandler(SSLConfig sslConfig,
      ByteBufAllocator alloc) {

    if (sslConfig != null && sslConfig.isSslEnabled()) {
      return new SslHandler(getOpenSslServerEngine(sslConfig, alloc));
    } else {
      return null;
    }
  }

}
