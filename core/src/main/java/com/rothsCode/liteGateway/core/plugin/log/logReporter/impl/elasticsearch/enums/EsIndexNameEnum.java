package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums;

import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.entity.GatewayLogEsEntity;
import lombok.Getter;

/**
 * es index name枚举
 *
 * @author rothscode
 * @version 1.0
 */
@Getter
public enum EsIndexNameEnum {

  /**
   * 网关日志
   */
  GATEWAY_LOG("gateway_log", GatewayLogEsEntity.class);

  private String name;
  private Class<?> clazz;

  EsIndexNameEnum(String indexName, Class<?> clazz) {
    this.name = indexName;
    this.clazz = clazz;
  }

  public static EsIndexNameEnum parseName(String indexName) {
    for (EsIndexNameEnum element : EsIndexNameEnum.values()) {
      if (indexName.equals(element.getName())) {
        return element;
      }
    }
    return null;
  }
}
