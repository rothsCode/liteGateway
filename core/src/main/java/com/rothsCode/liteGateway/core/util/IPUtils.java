package com.rothsCode.liteGateway.core.util;

import com.rothsCode.liteGateway.core.nettyServer.ProxyIPDecoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.Attribute;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * @author roths
 * @Description: clientIp获取类
 * @date 2023/8/22 14:47
 */
public class IPUtils {

  public static final String X_FORWARDED_FOR = "X-Forwarded-For";

  public static String getClientIP(ChannelHandlerContext ctx, HttpRequest request) {
    String clientIP = null;
    InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
    if (socketAddress != null) {
      clientIP = socketAddress.getAddress().getHostAddress();
    } else {
      Attribute<String> channelAttr = ctx.channel().attr(ProxyIPDecoder.key);
      if (null != channelAttr.get()) {
        clientIP = channelAttr.get();
      } else {
        String xForwardedValue = request.headers().get(X_FORWARDED_FOR);
        if (StringUtils.isNotEmpty(xForwardedValue)) {
          List<String> values = Arrays.asList(xForwardedValue.split(","));
          if (values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
            clientIP = values.get(0);
          }
        }
      }
    }
    return clientIP;
  }
}
