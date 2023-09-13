package com.rothsCode.liteGateway.core.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * @author roths
 * @Description:
 * @date 2023/8/17 11:41
 */
@Data
@Builder
public class ServiceInfo {

  private String serviceId;
  private String ip;
  private int port;
  private String serviceName;
  private Map<String, String> metadata = new HashMap();


}
