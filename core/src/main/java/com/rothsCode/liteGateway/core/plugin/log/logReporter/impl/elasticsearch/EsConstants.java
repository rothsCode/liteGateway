package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch;

/**
 * @author roths
 * @Description:
 * @date 2023/10/5 9:36
 */
public class EsConstants {

  /**
   * 逗号分隔符
   */
  public static final String COMMA = ",";
  /**
   * 冒号分隔符
   */
  public static final String COLON = ":";
  /**
   * 建立连接超时时间
   */
  public static int CONNECT_TIMEOUT_MILLIS = 1000;
  /**
   * 数据传输过程中的超时时间
   */
  public static int SOCKET_TIMEOUT_MILLIS = 30000;
  /**
   * 从连接池获取连接的超时时间
   */
  public static int CONNECTION_REQUEST_TIMEOUT_MILLIS = 500;
  /**
   * 路由节点的最大连接数
   */
  public static int MAX_CONN_PER_ROUTE = 10;
  /**
   * client最大连接数量
   */
  public static int MAX_CONN_TOTAL = 30;
  /**
   * index分片个数
   */
  public static String INDEX_NUMBER_OF_SHARDS_KEY = "index.number_of_shards";
  /**
   * index副本个数
   */
  public static String INDEX_NUMBER_OF_REPLICAS_KEY = "index.number_of_replicas";
  /**
   * index查询返回最大记录数
   */
  public static String INDEX_MAX_RESULT_WINDOW_KEY = "index.max_result_window";

  /**
   * index mapping属性
   */
  public static String INDEX_MAPPING_PROPERTIES_KEY = "properties";
  /**
   * index mapping类型
   */
  public static String INDEX_MAPPING_TYPE_KEY = "type";
  /**
   * index mapping格式
   */
  public static String INDEX_MAPPING_FORMAT_KEY = "format";
  /**
   * 时间格式
   */
  public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis";
  /**
   * index mapping 分词器
   */
  public static String INDEX_MAPPING_ANALYZER_KEY = "analyzer";
}
