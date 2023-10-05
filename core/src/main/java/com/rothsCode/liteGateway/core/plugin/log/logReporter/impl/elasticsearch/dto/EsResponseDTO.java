package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * es查询结果
 *
 * @author rothscode
 * @version 1.0
 */
@Data
public class EsResponseDTO {

  private long total;

  private JSONArray data;
}
