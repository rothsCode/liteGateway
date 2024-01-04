package com.rothsCode.liteGateway.core.plugin.core;

/**
 * rothscode 插件
 */
public enum PluginEnum {
  LOG_COLLECT("logCollect", "日志上报插件"),
  KAFKA("kafka", "kafka插件"),
  FILE_STORAGE("fileStorage", "文件存储插件"),
  HBASE("hbase", "hbase上报插件"),
  ES("elasticSearch", "elasticSearch上报插件"),
  JWT_AUTH("jwt", "jwt权限校验插件"),
  DISRUPTOR_PROCESS("disruptorProcess", "基于disruptor处理器插件"),
  ASYNC_NETTY_HTTP_CLIENT("asyncNettyHttpClient", "异步nettyhttp插件"),
  PROMETHEUS("prometheus", "prometheus指标统计插件");

  private String code;
  private String desc;

  PluginEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String getNameByCode(String code) {
    for (PluginEnum e : PluginEnum.values()) {
      if (e.getCode().equals(code)) {
        return e.getDesc();
      }
    }
    return null;
  }

  public String getCode() {
    return this.code;
  }

  public String getDesc() {
    return this.desc;
  }
}
