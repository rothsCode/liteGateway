package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.hbase;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.container.LifeCycle;
import com.rothsCode.liteGateway.core.plugin.log.logCollector.GatewayRequestLog;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.NamespaceNotFoundException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rothscode
 * @version 1.0
 */

public class HbaseClientServiceClient implements LifeCycle {

  public static final String NAME_SPACE = "GATEWAY_NAMESPACE";
  public static final String TABLE_NAME = "GATEWAY_REQUEST";
  /**
   * 列族名
   */
  public static final String COLUMN_FAMILY = "GATEWAY_REQUEST";
  private static final Logger log = LoggerFactory.getLogger(HbaseClientServiceClient.class);
  private static Connection connection;
  private static Admin admin;
  private ServerConfig serverConfig;
  private Configuration configuration;

  private HbaseClientServiceClient() {

  }

  public static HbaseClientServiceClient getInstance() {
    return HbaseClientServiceClient.SingletonHolder.INSTANCE;
  }

  @SneakyThrows
  @Override
  public void init() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    configuration = HBaseConfiguration.create();
    // 创建配置项,设置zookeeper的参数
    configuration.set("hbase.zookeeper.quorum", serverConfig.getHbaseZookeeperQuorum());
    configuration.set("hbase.zookeeper.property.clientPort", serverConfig.getHbaseZookeeperPort());
    connection = ConnectionFactory.createConnection(configuration);
    admin = connection.getAdmin();
    try {
      admin.getNamespaceDescriptor(NAME_SPACE);
    } catch (NamespaceNotFoundException e) {
      //若发生特定的异常，即找不到命名空间，则创建命名空间
      NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(NAME_SPACE).build();
      admin.createNamespace(namespaceDescriptor);
    }
    createTable(TABLE_NAME, COLUMN_FAMILY);
    log.info("HbaseClientServiceClient init");
  }

  @Override
  public void start() {

  }

  @Override
  public void shutDown() {
    if (connection != null) {
      try {
        admin.close();
        connection.close();
      } catch (IOException e) {
      }

    }
  }

  //创建表
  private void createTable(String tableName, String columnFamily) throws IOException {
    if (admin.tableExists(TableName.valueOf(tableName))) {
      return;
    }
    TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder
        .newBuilder(TableName.valueOf(tableName));
    ColumnFamilyDescriptorBuilder columnFamilyDescriptorBuilder = ColumnFamilyDescriptorBuilder
        .newBuilder(Bytes.toBytes(columnFamily));
    // 4. 构建列簇描述，构建表描述
    ColumnFamilyDescriptor cfDes = columnFamilyDescriptorBuilder.build();
    // 建立表和列簇的关联
    tableDescriptorBuilder.setColumnFamily(cfDes);
    TableDescriptor tableDescriptor = tableDescriptorBuilder.build();
    // 5. 创建表
    admin.createTable(tableDescriptor);
  }

  public List<GatewayRequestLog> getLogByScan(Map<String, Pair<String, String>> scanParam)
      throws IOException {
    // 1. 获取表
    Table table = connection.getTable(TableName.valueOf(TABLE_NAME));
    // 2. 构建scan请求对象
    Scan scan = new Scan();
    FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    for (Map.Entry<String, Pair<String, String>> entry : scanParam.entrySet()) {
      SingleColumnValueFilter startFilter = new SingleColumnValueFilter(
          Bytes.toBytes(COLUMN_FAMILY),
          Bytes.toBytes(entry.getKey()), CompareOperator.GREATER_OR_EQUAL,
          new BinaryComparator(Bytes.toBytes(entry.getValue().getKey())));
      SingleColumnValueFilter endFilter = new SingleColumnValueFilter(Bytes.toBytes(COLUMN_FAMILY),
          Bytes.toBytes(entry.getKey()), CompareOperator.LESS_OR_EQUAL,
          new BinaryComparator(Bytes.toBytes(entry.getValue().getValue())));
      filterList.addFilter(startFilter);
      filterList.addFilter(endFilter);
    }
    scan.setFilter(filterList);
    ResultScanner resultScanner = table.getScanner(scan);
    Iterator<Result> iterator = resultScanner.iterator();
    List<GatewayRequestLog> gatewayRequestLogs = new ArrayList<>();
    while (iterator.hasNext()) {
      Result result = iterator.next();
      GatewayRequestLog gatewayRequestLog = GatewayRequestLog.builder().build();
      parseResultCell(gatewayRequestLog, result);
      gatewayRequestLogs.add(gatewayRequestLog);
    }
    return gatewayRequestLogs;
  }

  @SneakyThrows
  public GatewayRequestLog getLogByRowKey(String rowKey) {
    GatewayRequestLog gatewayRequestLog = GatewayRequestLog.builder().build();
    Table table = connection.getTable(TableName.valueOf(TABLE_NAME));
    // 2. 使用rowkey构建Get对象
    Get get = new Get(Bytes.toBytes(rowKey));
    // 3. 执行get请求
    Result result = table.get(get);
    // 4. 解析所有单元格
    parseResultCell(gatewayRequestLog, result);
    return gatewayRequestLog;
  }

  private void parseResultCell(GatewayRequestLog gatewayRequestLog, Result result) {
    // 列出所有的单元格
    List<Cell> cellList = result.listCells();
    for (Cell cell : cellList) {
      // 将字节数组转换为字符串
      // 获取列的名称
      String columnName = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(),
          cell.getQualifierLength());
      // 获取值
      Object value;
      if ("gatewayStatus".equals(columnName) || "invokeStatus".equals(columnName)
          || "responseStatus".equals(columnName)) {
        value = Bytes
            .toInt(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
      } else {
        value = Bytes
            .toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
      }
      ReflectUtil.setFieldValue(gatewayRequestLog, columnName, value);
    }
  }

  public boolean batchSaveGatewayLog(List<GatewayRequestLog> gatewayLogEsList) {
    try {
      // 指定要插入的表
      Table table = connection.getTable(TableName.valueOf(TABLE_NAME));
      List<Put> putCollection = new ArrayList<>();
      for (GatewayRequestLog gatewayLogEsEntity : gatewayLogEsList) {
        Map<String, Object> columnMap = JSON
            .parseObject(JSON.toJSONString(gatewayLogEsEntity), Map.class);
        putCollection
            .add(buildPutOperation(COLUMN_FAMILY, gatewayLogEsEntity.getTraceId(), columnMap));
      }
      // 执行批量插入操作
      table.put(putCollection);
    } catch (Exception e) {
      log.error("batchSaveGatewayLogError:{}", e);
      return false;
    }
    return true;
  }

  public boolean saveGatewayLog(GatewayRequestLog gatewayRequestLog) {
    Map<String, Object> columnMap = JSON
        .parseObject(JSON.toJSONString(gatewayRequestLog), Map.class);
    return insert(TABLE_NAME, COLUMN_FAMILY, gatewayRequestLog.getTraceId(), columnMap);
  }

  public boolean insert(String tableName, String columnFamilyName, String rowKey,
      Map<String, Object> columnMap) {
    try {
      // 指定要插入的表
      Table table = connection.getTable(TableName.valueOf(tableName));
      // 构造插入要插入的数据
      Put put = buildPutOperation(columnFamilyName, rowKey, columnMap);
      // 执行插入操作
      table.put(put);
      return true;
    } catch (Exception e) {
      log.error("put hbase error:{}", e);
      return false;
    }
  }

  private Put buildPutOperation(String columnFamilyName, String rowKey,
      Map<String, Object> columnMap) {
    Put put = new Put(Bytes.toBytes(rowKey));
    // 对给列族加入对应的列
    Set<String> columnMapKeySet = columnMap.keySet();
    for (String columnName : columnMapKeySet) {
      // 添加列名与列值到HBase列中
      put.addColumn(
          Bytes.toBytes(columnFamilyName),
          Bytes.toBytes(columnName),
          getOriginColumnValue(columnMap, columnName)
      );
    }
    return put;
  }

  private byte[] getOriginColumnValue(Map<String, Object> columnMap, String columnName) {
    Object columnValue = columnMap.get(columnName);
    if (ObjectUtils.isEmpty(columnValue)) {
      return new byte[0];
    }
    if (columnValue instanceof String) {
      return Bytes.toBytes((String) columnValue);
    }
    if (columnValue instanceof Short) {
      return Bytes.toBytes((short) columnValue);
    }
    if (columnValue instanceof Integer) {
      return Bytes.toBytes((int) columnValue);
    }
    if (columnValue instanceof Long) {
      return Bytes.toBytes((long) columnValue);
    }
    if (columnValue instanceof Date) {
      long time = ((Date) columnValue).getTime();
      return Bytes.toBytes(time);
    }

    if (columnValue instanceof Float) {
      return Bytes.toBytes((float) columnValue);
    }
    if (columnValue instanceof Double) {
      return Bytes.toBytes((double) columnValue);
    }
    if (columnValue instanceof Boolean) {
      return Bytes.toBytes((boolean) columnValue);
    }
    if (columnValue instanceof BigDecimal) {
      return Bytes.toBytes((BigDecimal) columnValue);
    }
    if (columnValue instanceof ByteBuffer) {
      return Bytes.toBytes((ByteBuffer) columnValue);
    }
    if (columnValue instanceof ArrayList) {
      return Bytes.toBytes(JSONUtil.toJsonStr(columnValue));
    }
    throw new RuntimeException("columnName为空，无法进行数据类型转换失败");
  }

  private static class SingletonHolder {

    private static final HbaseClientServiceClient INSTANCE = new HbaseClientServiceClient();
  }

}
