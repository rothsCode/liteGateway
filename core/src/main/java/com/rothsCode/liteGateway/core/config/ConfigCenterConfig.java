package com.rothsCode.liteGateway.core.config;

import lombok.Data;

/**
 * @author roths
 * @Description: 配置中心配置
 * @date 2023/8/21 14:22
 */
@Data
public class ConfigCenterConfig {

  private String configType = "nacos";

  private String serverAddr;

  private String nameSpace;

  private String dataId;

  private String group;

}
