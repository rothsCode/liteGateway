package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.entity;

import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.annotation.EsDocument;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.annotation.EsField;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.annotation.EsId;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums.EsDataTypeEnum;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums.EsIndexNameEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author roths
 * @Description:
 * @date 2023/10/4 22:05
 */
@EsDocument(index = EsIndexNameEnum.GATEWAY_LOG)
@Data
@Builder
public class GatewayLogEsEntity {

  @EsId
  @EsField(type = EsDataTypeEnum.KEYWORD)
  private String traceId;
  @EsField(type = EsDataTypeEnum.KEYWORD)
  private String clientIp;
  @EsField(type = EsDataTypeEnum.KEYWORD)
  private String requestTime;
  @EsField(type = EsDataTypeEnum.KEYWORD)
  private String method;
  @EsField(type = EsDataTypeEnum.TEXT)
  private String requestHeader;
  @EsField(type = EsDataTypeEnum.TEXT)
  private String responseHeader;
  @EsField(type = EsDataTypeEnum.TEXT)
  private String queryParams;
  @EsField(type = EsDataTypeEnum.TEXT)
  private String requestBody;
  @EsField(type = EsDataTypeEnum.KEYWORD)
  private String requestUri;
  @EsField(type = EsDataTypeEnum.TEXT)
  private String responseBody;
  @EsField(type = EsDataTypeEnum.KEYWORD)
  private String rpcType;

  /**
   * 网关请求状态
   */
  @EsField(type = EsDataTypeEnum.INTEGER)
  private Integer gatewayStatus;

  /**
   * 调用接口状态
   */
  @EsField(type = EsDataTypeEnum.INTEGER)
  private Integer invokeStatus;

  /**
   * 接口返回业务状态
   */
  @EsField(type = EsDataTypeEnum.INTEGER)
  private Integer responseStatus;
  /**
   * 网关异常
   */
  @EsField(type = EsDataTypeEnum.TEXT)
  private String throwable;
  @EsField(type = EsDataTypeEnum.KEYWORD)
  private String host;
  @EsField(type = EsDataTypeEnum.KEYWORD)
  private String module;

}
