package com.rothsCode.liteGateway.core.pipeline.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rothscode
 * @Description: 事件处理上下文
 * @date 2022/7/30 17:47
 */
public class HandlerContext {

  /**
   * 存储对象
   */
  private Map<String, Object> handleParam = new HashMap<>();

  /**
   * 向公共参数中添加参数
   *
   * @param key   存入参数的key
   * @param value 存入参数的值
   */
  public void put(String key, Object value) {
    this.handleParam.put(key, value);
  }

  /**
   * 获取java对象的方法
   *
   * @param key 存入key
   * @param <T> 转换后的java对象，加入了强制转换的操作
   * @return
   */
  public <T> T getObject(String key) {
    return (T) this.handleParam.get(key);
  }

  /**
   * 进行对象的移除操作
   *
   * @param key
   */
  public void remove(String key) {
    this.handleParam.remove(key);
  }

  /**
   * 当值不存在时需要返回返回值的类型
   *
   * @param key 存入的key信息
   * @param def 默认的值信息，需要与存入的类型相同，否则会报错误
   * @return
   */
  public <T> T getValueOrDef(String key, T def) {
    T value = (T) this.handleParam.get(key);

    if (null != value) {
      return value;
    } else {
      return def;
    }
  }

  /**
   * 进行清理操作
   */
  public void cleanParam() {
    this.handleParam.clear();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("HandlerContext{");
    sb.append("handleParam=").append(handleParam);
    sb.append('}');
    return sb.toString();
  }

}
