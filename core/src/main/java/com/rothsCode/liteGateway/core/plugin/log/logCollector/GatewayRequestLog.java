/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rothsCode.liteGateway.core.plugin.log.logCollector;

import lombok.Builder;
import lombok.Data;

/**
 * 网关请求日志
 */
@Builder
@Data
public class GatewayRequestLog {

  private String clientIp;

  private String requestTime;

  private String method;

  private String requestHeader;

  private String responseHeader;

  private String queryParams;

  private String requestBody;

  private String requestUri;

  private String responseBody;

  private String rpcType;

  /**
   * 网关请求状态
   */
  private Integer gatewayStatus;

  /**
   * 调用接口状态
   */
  private Integer invokeStatus;

  /**
   * 接口返回业务状态
   */
  private Integer responseStatus;

  /**
   * 网关异常
   */
  private String throwable;

  private String host;

  private String module;

  private String traceId;

  private String path;


}
