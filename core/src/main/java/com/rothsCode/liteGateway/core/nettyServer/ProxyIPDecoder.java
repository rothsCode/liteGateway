package com.rothsCode.liteGateway.core.nettyServer;

/**
 * @author roths
 * @Description:
 * @date 2023/8/31 14:46
 */

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import java.nio.charset.Charset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description nginx代理netty tcp服务端负载均衡，nginx stream要打开 proxy_protocol on; 配置
 */

public class ProxyIPDecoder extends ByteToMessageDecoder {

  /**
   * 保存客户端IP
   */
  public static AttributeKey<String> key = AttributeKey.valueOf("IP");
  private Logger logger = LoggerFactory.getLogger(ProxyIPDecoder.class);

  /**
   * decode() 会根据接收的数据，被调用多次，直到确定没有新的元素添加到list, 或者是 ByteBuf 没有更多的可读字节为止。 如果 list 不为空，就会将 list
   * 的内容传递给下一个 handler
   *
   * @param ctx     上下文对象
   * @param byteBuf 入站后的 ByteBuf
   * @param out     将解码后的数据传递给下一个 handler
   * @throws Exception
   */
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out)
      throws Exception {
    /*消息打印--------------------------*/
    byte[] bytes = printSz(byteBuf);
    String message = new String(bytes, Charset.forName("UTF-8"));
    if (bytes.length > 0) {
      //判断是否有代理
      if (message.indexOf("PROXY") != -1) {
        if (message.indexOf("\n") != -1) {
          String[] str = message.split("\n")[0].split(" ");
          Attribute<String> channelAttr = ctx.channel().attr(key);
          //基于channel的属性
          if (null == channelAttr.get()) {
            channelAttr.set(str[2]);
          }
        }
        //清空数据，重要不能省略
        byteBuf.clear();
      }
      if (byteBuf.readableBytes() > 0) {
        out.add(byteBuf.readBytes(byteBuf.readableBytes()));
      }
    }
  }


  /**
   * 打印byte数组
   *
   * @param newBuf
   */
  public byte[] printSz(ByteBuf newBuf) {
    ByteBuf copy = newBuf.copy();
    byte[] bytes = new byte[copy.readableBytes()];
    copy.readBytes(bytes);
    ReferenceCountUtil.release(copy);
    return bytes;
  }
}