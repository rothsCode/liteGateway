package com.rothsCode.liteGateway.core.config;

import lombok.Data;

/**
 * @author roths
 * @Description: 注册中心配置
 * @date 2023/8/21 14:22
 */
@Data
public class RegisterConfig {

  private String registerType = "nacos";

  private String serverAddr;

  private String nameSpace;

  private String group;

  private String userName;

  private String password;
  /**
   * 服务负载均衡执行策略
   */
  private String loadBalanceStrategy = "round";
}
