package com.rothsCode.liteGateway.core.util;

import com.rothsCode.liteGateway.core.constants.GatewayHeaderConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author roths
 * @Description: traceId工具类
 * @date 2023/8/22 14:47
 */
public class MdcUtil {

  public static final Logger log = LoggerFactory.getLogger(MdcUtil.class);
  /**
   * 用户自定义标识
   */
  private static final String USER_CUSTOMIZED_FLAG_KEY = "USER_CUSTOMIZED_FLAG";
  private static final String USER_CUSTOMIZED_FLAG_VALUE = "true";

  private MdcUtil() {
  }

  /**
   * 获取当前线程的traceId
   *
   * @return
   */
  public static String getTraceId() {
    return MDC.get(GatewayHeaderConstant.TRACE_ID);
  }

  /**
   * 设置当前线程的traceId
   *
   * @return
   */
  public static void setTraceId(String traceId) {
    if (!StringUtils.isBlank(traceId)) {
      MDC.put(GatewayHeaderConstant.TRACE_ID, traceId);
    } else {
      MDC.remove(GatewayHeaderConstant.TRACE_ID);
    }
  }

  /**
   * 获取当前线程的traceId
   *
   * @return
   */
  public static String getUkId() {
    return String.valueOf(SnowFlake.generateId());
  }

  /**
   * 初始化当前线程的traceId
   *
   * @return
   */
  public static void initTraceId() {
    String id = String.valueOf(SnowFlake.generateId());
    MDC.put(GatewayHeaderConstant.TRACE_ID, id);
  }

  /**
   * 初始化当前线程的traceId,根据父线程的traceId
   *
   * @return
   */
  public static void initTraceId(String parentTraceId) {
    String id = parentTraceId + SnowFlake.generateId();
    MDC.put(GatewayHeaderConstant.TRACE_ID, id);
  }

  /**
   * 获取当前线程的traceId,如果没有，初始化
   *
   * @return
   */
  public static String getOrInitTraceId() {
    String id = MDC.get(GatewayHeaderConstant.TRACE_ID);
    if (StringUtils.isBlank(id)) {
      id = String.valueOf(SnowFlake.generateId());
      MDC.put(GatewayHeaderConstant.TRACE_ID, id);
    }
    return id;
  }

  /**
   * 设置当前线程的traceId
   *
   * @return
   */
  public static void setUserTraceId(String traceId) {
    if (StringUtils.isBlank(traceId)) {
      traceId = SnowFlake.generateIdStr();
    }
    MDC.put(GatewayHeaderConstant.TRACE_ID, traceId);
    MDC.put(USER_CUSTOMIZED_FLAG_KEY, USER_CUSTOMIZED_FLAG_VALUE);
  }

  /**
   * 设置当前线程的traceId
   *
   * @return
   */
  public static Boolean isUserCustomized() {
    String value = MDC.get(USER_CUSTOMIZED_FLAG_KEY);
    if (StringUtils.isNotEmpty(value) && value.equals(USER_CUSTOMIZED_FLAG_VALUE)) {
      return true;
    }
    return false;
  }

  /**
   * 移除当前线程的traceId
   */
  public static void removeTraceId() {
    MDC.remove(GatewayHeaderConstant.TRACE_ID);
  }

  /**
   * 清除
   */
  public static void clear() {
    MDC.clear();
  }
}
