package com.rothsCode.liteGateway.core.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author roths
 * @Description:dubbo泛化调用服务端信息
 * @date 2023/8/22 20:02
 */
@Data
@Builder
public class DubboServiceInvoker {

  /**
   * 接口调用路径
   */
  private String apiPath;
  /**
   * 接口名
   */
  private String interfaceName;

  /**
   * 接口名
   */
  private String methodName;

  /**
   * 参数类型
   */
  private String[] paramTypes;

  /**
   * 参数值
   */
  private Object[] paramValues;

  /**
   * 服务版本号
   */
  private String version;


}
