package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch;

import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.COLON;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.COMMA;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.CONNECTION_REQUEST_TIMEOUT_MILLIS;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.CONNECT_TIMEOUT_MILLIS;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.DATE_FORMAT;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.INDEX_MAPPING_ANALYZER_KEY;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.INDEX_MAPPING_FORMAT_KEY;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.INDEX_MAPPING_PROPERTIES_KEY;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.INDEX_MAPPING_TYPE_KEY;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.INDEX_MAX_RESULT_WINDOW_KEY;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.INDEX_NUMBER_OF_REPLICAS_KEY;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.INDEX_NUMBER_OF_SHARDS_KEY;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.MAX_CONN_PER_ROUTE;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.MAX_CONN_TOTAL;
import static com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.EsConstants.SOCKET_TIMEOUT_MILLIS;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.container.LifeCycle;
import com.rothsCode.liteGateway.core.exception.EsException;
import com.rothsCode.liteGateway.core.exception.GatewayRequestStatusEnum;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.annotation.EsDocument;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.annotation.EsField;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.annotation.EsId;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.dto.EsResponseDTO;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums.EsAnalyzerEnum;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums.EsDataTypeEnum;
import com.rothsCode.liteGateway.core.plugin.log.logReporter.impl.elasticsearch.enums.EsIndexNameEnum;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rothscode
 * @version 1.0
 */
public class EsClientServiceClient implements LifeCycle {

  private static final Logger log = LoggerFactory.getLogger(EsClientServiceClient.class);

  private ServerConfig serverConfig;

  private RestHighLevelClient restHighLevelClient;

  private EsClientServiceClient() {

  }

  public static EsClientServiceClient getInstance() {
    return EsClientServiceClient.SingletonHolder.INSTANCE;
  }

  @SneakyThrows
  @Override
  public void init() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    // 创建restClient的构造器
    RestClientBuilder restClientBuilder = RestClient
        .builder(loadHttpHosts(serverConfig.getEsClusterNodes()));
    // 设置连接超时时间等参数
    setConnectTimeOutConfig(restClientBuilder);
    setConnectConfig(restClientBuilder);
    //用户鉴权
    setAuthConfig(restClientBuilder);
    restHighLevelClient = new RestHighLevelClient(restClientBuilder);
    //索引不存在则创建索引
    createIndexIfNotExists(EsIndexNameEnum.GATEWAY_LOG);
  }

  @Override
  public void start() {

  }

  @Override
  public void shutDown() {
    if (restHighLevelClient != null) {
      try {
        log.info("Closing the ES REST client");
        restHighLevelClient.close();
      } catch (IOException e) {
        log.error("error when closing the ES REST client:{}", e);
      }
    }
  }

  /**
   * create index
   */
  public boolean createIndex(Class<?> clazz) throws Exception {
    EsDocument esDocument = clazz.getAnnotation(EsDocument.class);
    checkClassEsDocumentNonNull(esDocument, clazz);

    String indexName = indexName(esDocument);
    return doCreateIndex(clazz, indexName, null);
  }

  /**
   * create index
   */
  private boolean doCreateIndex(Class<?> clazz, String indexName, Set<String> excludeFields)
      throws Exception {
    CreateIndexRequest request = new CreateIndexRequest(indexName);

    // 1、设置shard数量，副本数，return_window_size
    Settings.Builder settingsBuilder = Settings.builder()
        .put(INDEX_NUMBER_OF_SHARDS_KEY, serverConfig.getEsShardsNumber())
        .put(INDEX_NUMBER_OF_REPLICAS_KEY, serverConfig.getEsReplicasNumber())
        .put(INDEX_MAX_RESULT_WINDOW_KEY, serverConfig.getReturnSize());
    // 2、自定义一个ik_max_word+edge_ngram的分词器
    customizeIkMaxWordPlusEdgeNGramAnalyze(settingsBuilder);

    request.settings(settingsBuilder);

    request.mapping(generateBuilder(clazz, excludeFields));
    CreateIndexResponse response = restHighLevelClient.indices()
        .create(request, RequestOptions.DEFAULT);
    // 3、确认请求
    boolean acknowledged = response.isAcknowledged();
    boolean shardsAcknowledged = response.isShardsAcknowledged();
    return acknowledged || shardsAcknowledged;
  }

  /**
   * 自定义一个ik_max_word+edge_ngram的分词器
   */
  private void customizeIkMaxWordPlusEdgeNGramAnalyze(Settings.Builder settingsBuilder) {
    //定义一个名称为ikmax_edgengram_filter的edge_ngram过滤器
    settingsBuilder
        .put("analysis.filter.ikmax_edgengram_filter.type", "edge_ngram")
        .put("analysis.filter.ikmax_edgengram_filter.min_gram", 1)
        .put("analysis.filter.ikmax_edgengram_filter.max_gram", 30)
        //定义一个ikmax_edgengram的分词器，使用ikmax_edgengram_filter
        .put("analysis.analyzer.ikmax_edgengram.type", "custom")
        .put("analysis.analyzer.ikmax_edgengram.tokenizer", "ik_max_word")
        .putList("analysis.analyzer.ikmax_edgengram.filter", "lowercase", "ikmax_edgengram_filter");
  }

  /**
   * create index if not exists
   */
  public boolean createIndexIfNotExists(Class<?> clazz) throws Exception {
    EsDocument esDocument = clazz.getAnnotation(EsDocument.class);
    checkClassEsDocumentNonNull(esDocument, clazz);
    String indexName = indexName(esDocument);

    // 判断es是否已经创建
    boolean indexExists = isIndexExists(esDocument.index());
    if (indexExists) {
      return false;
    }
    // 如果没有，进行创建
    return doCreateIndex(clazz, indexName, null);
  }

  /**
   * create index if not exists
   */
  public boolean createIndexIfNotExists(EsIndexNameEnum esIndexName) throws Exception {
    String indexName = esIndexName.getName();

    // 判断es是否已经创建
    boolean indexExists = isIndexExists(esIndexName);
    if (indexExists) {
      return false;
    }
    // 如果没有，进行创建
    return doCreateIndex(esIndexName.getClazz(), indexName, null);
  }

  /**
   * update index
   */
  public boolean updateIndex(Class<?> clazz) throws Exception {
    EsDocument esDocument = clazz.getAnnotation(EsDocument.class);
    checkClassEsDocumentNonNull(esDocument, clazz);

    String indexName = indexName(esDocument);
    PutMappingRequest request = new PutMappingRequest(indexName);
    request.source(generateBuilder(clazz, null));
    AcknowledgedResponse response = restHighLevelClient.indices()
        .putMapping(request, RequestOptions.DEFAULT);
    return response.isAcknowledged();
  }

  /**
   * delete index
   */
  public boolean deleteIndex(EsIndexNameEnum esIndexName) {
    boolean acknowledged = false;
    try {
      String indexName = esIndexName.getName();
      DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
      deleteIndexRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
      AcknowledgedResponse response = restHighLevelClient.indices()
          .delete(deleteIndexRequest, RequestOptions.DEFAULT);
      acknowledged = response.isAcknowledged();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return acknowledged;
  }

  /**
   * index a document
   */
  public IndexResponse index(Object o) throws Exception {
    Class<?> clazz = o.getClass();
    EsDocument esDocument = clazz.getDeclaredAnnotation(EsDocument.class);
    checkClassEsDocumentNonNull(esDocument, clazz);
    String indexName = indexName(esDocument);
    IndexRequest request = new IndexRequest(indexName);
    Field field = getFieldByAnnotation(o, EsId.class);
    if (field != null) {
      field.setAccessible(true);
      Object id = field.get(o);
      request = request.id(id.toString());
    }
    String userJson = JSON.toJSONString(o);
    request.source(userJson, XContentType.JSON);
    return restHighLevelClient.index(request, RequestOptions.DEFAULT);
  }

  /**
   * 如果已存在，不索引
   */
  public IndexResponse indexIfNotExists(Object o) throws Exception {
    Class<?> clazz = o.getClass();
    EsDocument esDocument = clazz.getDeclaredAnnotation(EsDocument.class);
    checkClassEsDocumentNonNull(esDocument, clazz);

    String indexName = indexName(esDocument);
    IndexRequest request = new IndexRequest(indexName);
    Field field = getFieldByAnnotation(o, EsId.class);
    if (field != null) {
      field.setAccessible(true);
      Object id = field.get(o);
      if (null != id && isDocumentExists(esDocument.index(), id.toString())) {
        throw new Exception(
            String.format("文档已存在，index name = %s, document id = %s", indexName, id));
      }
      request = request.id(id.toString());
    }
    String userJson = JSON.toJSONString(o);
    request.source(userJson, XContentType.JSON);
    return restHighLevelClient.index(request, RequestOptions.DEFAULT);
  }

  /**
   * 批量索引
   */
  public <T> boolean bulkIndex(List<T> objects) throws Exception {
    BulkRequest bulkRequest = new BulkRequest();
    for (Object o : objects) {
      EsDocument esDocument = o.getClass().getDeclaredAnnotation(EsDocument.class);
      checkClassEsDocumentNonNull(esDocument, o.getClass());
      String indexName = indexName(esDocument);
      IndexRequest request = new IndexRequest(indexName);
      Field field = getFieldByAnnotation(o, EsId.class);
      if (field != null) {
        field.setAccessible(true);
        Object id = field.get(o);
        request = request.id(id.toString());
      }
      String userJson = JSON.toJSONString(o);
      request.source(userJson, XContentType.JSON);
      bulkRequest.add(request);
    }
    log.info("bulkIndex->bulkRequest={}", JSONObject.toJSONString(bulkRequest.requests()));
    BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    for (BulkItemResponse bulkItemResponse : bulkResponse) {
      if (bulkItemResponse.isFailed()) {
        BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
        log.error(failure.toString());
        return false;
      }
    }
    return true;
  }

  /**
   * bulk 并发版本控制操作
   *
   * @param objects   对象
   * @param timestamp 时间戳
   */
  public <T> void bulkIndexWithVersionControl(List<T> objects, long timestamp) throws Exception {
    BulkRequest bulkRequest = new BulkRequest();
    for (Object o : objects) {
      EsDocument esDocument = o.getClass().getDeclaredAnnotation(EsDocument.class);
      checkClassEsDocumentNonNull(esDocument, o.getClass());
      String indexName = indexName(esDocument);
      IndexRequest request = new IndexRequest(indexName);
      Field field = getFieldByAnnotation(o, EsId.class);
      if (field != null) {
        field.setAccessible(true);
        Object id = field.get(o);
        if (null == id) {
          throw new Exception(String.format("缺少文档ID，index name = %s", indexName));
        }
        request = request.id(id.toString());
        request.version(timestamp);
        request.versionType(VersionType.EXTERNAL);
      }
      String userJson = JSON.toJSONString(o);
      request.source(userJson, XContentType.JSON);
      bulkRequest.add(request);
    }
    log.info("bulkIndex->bulkRequest={}", JSONObject.toJSONString(bulkRequest.requests()));
    BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    for (BulkItemResponse bulkItemResponse : bulkResponse) {
      if (bulkItemResponse.isFailed()) {
        BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
        log.error("bulkIndexWithVersionControl，err={}", failure.toString());
      }
    }
  }

  /**
   * query document by id
   */
  public String queryById(EsIndexNameEnum esIndexName, String id) throws IOException {
    GetRequest getRequest = new GetRequest(esIndexName.getName(), id);
    GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
    return getResponse.getSourceAsString();
  }

  /**
   * search the index with #SearchSourceBuilder, return result as json string
   */
  public EsResponseDTO search(EsIndexNameEnum esIndexName, SearchSourceBuilder searchSourceBuilder)
      throws IOException {
    // 获取返回hits
    SearchHits hits = search_(esIndexName, searchSourceBuilder);

    JSONArray jsonArray = new JSONArray();
    for (SearchHit hit : hits) {
      String sourceAsString = hit.getSourceAsString();
      JSONObject jsonObject = JSONObject.parseObject(sourceAsString);
      jsonArray.add(jsonObject);
    }

    EsResponseDTO esResponseDTO = new EsResponseDTO();
    esResponseDTO.setTotal(hits.getHits().length);
    esResponseDTO.setData(jsonArray);
    return esResponseDTO;
  }

  /**
   * search the index with #SearchSourceBuilder, return SearchHits
   */
  public SearchHits search_(EsIndexNameEnum esIndexName, SearchSourceBuilder searchSourceBuilder)
      throws IOException {
    SearchRequest searchRequest = new SearchRequest(esIndexName.getName());
    searchRequest.source(searchSourceBuilder);
    if (searchSourceBuilder.from() == -1) {
      searchSourceBuilder.size(serverConfig.getSearchResultSize());
    }

    // 查询es
    SearchResponse searchResponse = restHighLevelClient
        .search(searchRequest, RequestOptions.DEFAULT);

    // 获取返回hits
    SearchHits hits = searchResponse.getHits();
    return hits;
  }

  /**
   * 进行es聚合查询
   */
  public Aggregations aggregation(String indexName, SearchSourceBuilder searchSourceBuilder)
      throws Exception {
    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.source(searchSourceBuilder);
    SearchResponse searchResponse = restHighLevelClient
        .search(searchRequest, RequestOptions.DEFAULT);
    return searchResponse.getAggregations();
  }

  /**
   * 根据aggregationsName进行聚合查询
   */
  public Map<String, Long> searchAggregationsGroupCount(String indexName, String aggregationsName,
      SearchSourceBuilder searchSourceBuilder) throws IOException {
    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.source(searchSourceBuilder);
    SearchResponse searchResponse = restHighLevelClient
        .search(searchRequest, RequestOptions.DEFAULT);
    ParsedTerms termsName = searchResponse.getAggregations().get(aggregationsName);

    SortedMap<String, Long> map = new TreeMap<String, Long>();
    List<? extends Terms.Bucket> buckets = termsName.getBuckets();
    buckets.forEach(bucket -> {
      map.put((String) bucket.getKey(), bucket.getDocCount());
    });
    return map;
  }

  /**
   * 已存在则更新，不存在则插入
   */
  public boolean saveOrUpdate(Object o) throws Exception {
    Class<?> clazz = o.getClass();
    EsDocument esDocument = clazz.getDeclaredAnnotation(EsDocument.class);
    checkClassEsDocumentNonNull(esDocument, clazz);

    String indexName = indexName(esDocument);
    Field field = getFieldByAnnotation(o, EsId.class);
    if (null == field) {
      throw new Exception(String.format("缺少文档ID，index name = %s", indexName));
    }
    field.setAccessible(true);
    Object id = field.get(o);
    String userJson = JSON.toJSONString(o);
    IndexRequest indexRequest = new IndexRequest(indexName).source(userJson, XContentType.JSON);
    UpdateRequest updateRequest = new UpdateRequest(indexName, id.toString())
        .doc(userJson, XContentType.JSON).upsert(indexRequest);
    UpdateResponse updateResponse = restHighLevelClient
        .update(updateRequest, RequestOptions.DEFAULT);

    String index = updateResponse.getIndex();
    String docId = updateResponse.getId();
    long version = updateResponse.getVersion();
    log.info(
        String.format("Document update: index=%s, docId=%s, version=%s", index, docId, version));
    return updateResponse.getResult() == Result.CREATED
        || updateResponse.getResult() == Result.UPDATED
        || updateResponse.getResult() == Result.NOOP;
  }

  /**
   * 批量删除
   */
  public boolean batchDelete(EsIndexNameEnum esIndexName, List<String> docIds) throws Exception {
    if (!isIndexExists(esIndexName)) {
      throw new Exception("Index not exist, index name = " + esIndexName.getName());
    }
    if (CollectionUtil.isEmpty(docIds)) {
      throw new Exception("empty doc id list");
    }
    BulkRequest request = new BulkRequest();

    for (String id : docIds) {
      DeleteRequest deleteRequest = new DeleteRequest(esIndexName.getName(), id);
      request.add(deleteRequest);
    }

    BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);

    for (BulkItemResponse bulkItemResponse : bulkResponse) {
      DocWriteResponse itemResponse = bulkItemResponse.getResponse();
      DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
      log.info("item response of batch delete：{}", deleteResponse);
      if (bulkItemResponse.isFailed()) {
        log.error("item delete failed {}", bulkItemResponse.getFailureMessage());
        return false;
      }
    }
    return true;
  }

  /**
   * 批量save or update
   */
  public <T> boolean batchSaveOrUpdate(List<T> list) throws Exception {
    if (CollectionUtil.isEmpty(list)) {
      throw new Exception("empty doc list");
    }

    Object o = list.get(0);
    Class<?> clazz = o.getClass();
    EsDocument esDocument = clazz.getDeclaredAnnotation(EsDocument.class);
    checkClassEsDocumentNonNull(esDocument, clazz);
    String indexName = indexName(esDocument);

    BulkRequest request = new BulkRequest();
    for (Object obj : list) {
      String jsonStr = JSON.toJSONString(obj);
      IndexRequest indexReq = new IndexRequest(indexName).source(jsonStr, XContentType.JSON);

      Field fieldByAnnotation = getFieldByAnnotation(obj, EsId.class);
      if (fieldByAnnotation != null) {
        fieldByAnnotation.setAccessible(true);
        Object id = fieldByAnnotation.get(obj);
        indexReq = indexReq.id(id.toString());
      }
      request.add(indexReq);
    }

    BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);

    for (BulkItemResponse bulkItemResponse : bulkResponse) {
      DocWriteResponse itemResponse = bulkItemResponse.getResponse();
      IndexResponse indexResponse = (IndexResponse) itemResponse;
      log.info("item response：{}", indexResponse);
      if (bulkItemResponse.isFailed()) {
        log.error("es response error : {}", bulkItemResponse.getFailureMessage());
        return false;
      }
    }
    return true;
  }

  /**
   * delete doc
   */
  public boolean deleteDoc(EsIndexNameEnum esIndexName, String docId) throws IOException {
    DeleteRequest request = new DeleteRequest(esIndexName.getName(), docId);
    DeleteResponse deleteResponse = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
    ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
    if (shardInfo.getFailed() > 0) {
      for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
        String reason = failure.reason();
        log.error("delete doc error：{}, docId:{}", reason, docId);
      }
    }
    return true;
  }

  /**
   * update doc by json data
   */
  public boolean updateDoc(EsIndexNameEnum esIndexName, String docId, Object o) throws IOException {
    UpdateRequest request = new UpdateRequest(esIndexName.getName(), docId);
    request.doc(JSON.toJSONString(o), XContentType.JSON);
    UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
    String index = updateResponse.getIndex();
    String id = updateResponse.getId();
    long version = updateResponse.getVersion();
    return updateResponse.getResult() == Result.CREATED
        || updateResponse.getResult() == Result.UPDATED;
  }

  /**
   * update doc by map data
   */
  public boolean updateDoc(EsIndexNameEnum esIndexName, String docId, Map<String, Object> map)
      throws IOException {
    UpdateRequest request = new UpdateRequest(esIndexName.getName(), docId);
    request.doc(map);
    UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
    String index = updateResponse.getIndex();
    String id = updateResponse.getId();
    long version = updateResponse.getVersion();
    log.info(String.format("Document update: index=%s, docId=%s, version=%s", index, id, version));
    return updateResponse.getResult() == Result.CREATED
        || updateResponse.getResult() == Result.UPDATED;
  }

  /**
   * index exists or not
   */
  public boolean isIndexExists(EsIndexNameEnum esIndexName) {
    boolean exists = false;
    GetIndexRequest getIndexRequest = new GetIndexRequest(esIndexName.getName());
    getIndexRequest.humanReadable(true);
    try {
      exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      //ignore
    }
    return exists;
  }

  /**
   * 判断文档是否存在
   */
  public boolean isDocumentExists(EsIndexNameEnum esIndexName, String docId) throws IOException {
    GetRequest request = new GetRequest(esIndexName.getName(), docId);
    return restHighLevelClient.exists(request, RequestOptions.DEFAULT);
  }

  /**
   * 构造es文档，返回内容构造器
   *
   * @param excludeFields 构建index时排除一些field
   */
  private XContentBuilder generateBuilder(Class<?> clazz, Set<String> excludeFields)
      throws Exception {
    if (null == excludeFields) {
      excludeFields = new HashSet<>();
    }
    XContentBuilder builder = XContentFactory.jsonBuilder();
    builder.startObject();
    builder.startObject(INDEX_MAPPING_PROPERTIES_KEY);
    Field[] fields = clazz.getDeclaredFields();

    // 遍历clazz的每个field
    for (Field field : fields) {
      if (excludeFields.contains(field.getName())) {
        continue;
      }
      if (field.isAnnotationPresent(EsField.class)) {
        EsField esField = field.getDeclaredAnnotation(EsField.class);
        if (esField.type() == EsDataTypeEnum.OBJECT) {
          // 处理嵌套的Object类型
          generateObjectBuilder(builder, field);
        } else {
          // 处理普通es数据类型
          builder.startObject(field.getName());
          builder.field(INDEX_MAPPING_TYPE_KEY, esField.type().getType());
          if (esField.type() == EsDataTypeEnum.TEXT) {
            handleTextTypeFieldMapping(builder, esField);
          }
          if (esField.type() == EsDataTypeEnum.DATE) {
            builder.field(INDEX_MAPPING_FORMAT_KEY, DATE_FORMAT);
          }
          builder.endObject();
        }
      }
    }
    builder.endObject();
    builder.endObject();
    log.info(builder.toString());
    return builder;
  }

  /**
   * 处理嵌套的Object类型
   */
  private void generateObjectBuilder(XContentBuilder builder, Field objectField) throws Exception {
    Class<?> type = objectField.getType();
    Field[] fields = type.getDeclaredFields();
    builder.startObject(objectField.getName());
    builder.startObject(INDEX_MAPPING_PROPERTIES_KEY);
    // 遍历该对象属性
    for (Field field : fields) {
      if (field.isAnnotationPresent(EsField.class)) {
        EsField esField = field.getDeclaredAnnotation(EsField.class);
        builder.startObject(field.getName());
        builder.field(INDEX_MAPPING_TYPE_KEY, esField.type().getType());
        // keyword 不需要分词
        if (esField.type() == EsDataTypeEnum.TEXT) {
          handleTextTypeFieldMapping(builder, esField);
        }
        if (esField.type() == EsDataTypeEnum.DATE) {
          builder.field(INDEX_MAPPING_FORMAT_KEY, DATE_FORMAT);
        }
        builder.endObject();
      }
    }
    builder.endObject();
    builder.endObject();
  }

  /**
   * 处理Text类型field的mapping
   */
  private void handleTextTypeFieldMapping(XContentBuilder builder, EsField esField)
      throws IOException {
    if (esField.analyzer().equals(EsAnalyzerEnum.IK_MAX_WORD_NGRAM)) {
      builder.field(INDEX_MAPPING_ANALYZER_KEY, "ikmax_edgengram");
      builder.field("search_analyzer", "ik_max_word");
    } else {
      builder.field(INDEX_MAPPING_ANALYZER_KEY, esField.analyzer().getType());
    }
  }

  /**
   * 获取class指定注解field
   */
  private Field getFieldByAnnotation(Object o, Class<? extends Annotation> annotationClass) {
    Class<?> clazz = o.getClass();
    List<Field[]> allFields = new ArrayList<>();
    while (clazz != null) {
      // 遍历所有父类字节码对象
      // 获取字节码对象的属性对象数组
      Field[] declaredFields = clazz.getDeclaredFields();
      allFields.add(declaredFields);
      // 获得父类的字节码对象
      clazz = clazz.getSuperclass();
    }
    if (allFields.size() > 0) {
      for (Field[] fields : allFields) {
        for (Field field : fields) {
          if (field.isAnnotationPresent(annotationClass)) {
            return field;
          }
        }
      }
    }
    return null;
  }

  /**
   * 索引重建: 注意仅限于dev环境使用
   *
   * @param excludeFields 重建索引的时候，需要排除的字段，当field的type有问题的时候，可以使用这个参数进行排除
   */
  public boolean reIndex(Class<?> indexClazz, Set<String> excludeFields) throws Exception {
    EsDocument esDocument = indexClazz.getAnnotation(EsDocument.class);
    checkClassEsDocumentNonNull(esDocument, indexClazz);

    String indexName = indexName(esDocument);

    boolean exists = isIndexExists(esDocument.index());
    if (!exists) {
      //不存在，直接新建索引即可
      createIndex(indexClazz);
      return true;
    }

    //1、获取原始index的全量数据
    JSONArray fullData = getFullDataByScroll(indexName);

    //2、删除索引,重新建立索引
    deleteIndex(esDocument.index());
    doCreateIndex(indexClazz, indexName, null);

    //3、将全量数据重新插入新建的索引里面去
    for (int i = 0; i < fullData.size(); i++) {
      index(JSONObject.parseObject(fullData.getString(i), indexClazz));
    }
    return true;
  }

  /**
   * 通过scroll查询index全量数据
   */
  private JSONArray getFullDataByScroll(String indexName) throws Exception {
    JSONArray result = new JSONArray();

    final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
    // 新建索引搜索请求
    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.scroll(scroll);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(matchAllQuery());
    // 设定每次返回多少条数据
    searchSourceBuilder.size(5000);
    searchRequest.source(searchSourceBuilder);

    // 查询一次
    SearchResponse searchResponse = restHighLevelClient
        .search(searchRequest, RequestOptions.DEFAULT);

    SearchHit[] searchHits = searchResponse.getHits().getHits();

    for (SearchHit searchHit : searchHits) {
      String sourceAsString = searchHit.getSourceAsString();
      result.add(JSONObject.parseObject(sourceAsString));
    }

    // 循环遍历，直到没有数据
    String scrollId = searchResponse.getScrollId();
    while (searchHits != null && searchHits.length > 0) {
      SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
      scrollRequest.scroll(scroll);
      // 使用scroll查询
      searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
      scrollId = searchResponse.getScrollId();
      searchHits = searchResponse.getHits().getHits();
      if (searchHits != null && searchHits.length > 0) {
        for (SearchHit searchHit : searchHits) {
          String sourceAsString = searchHit.getSourceAsString();
          result.add(JSONObject.parseObject(sourceAsString));
        }
      }
    }

    // 清除scroll
    ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
    clearScrollRequest.addScrollId(scrollId);
    restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
    return result;
  }

  /**
   * 获取es文档名称
   */
  private String indexName(EsDocument esDocument) {
    return esDocument.index().getName();
  }

  /**
   * 检查EsDocument的存在性
   */
  private void checkClassEsDocumentNonNull(EsDocument esDocument, Class<?> clazz)
      throws EsException {
    if (null == esDocument) {
      throw new EsException(GatewayRequestStatusEnum.ES_INDEX_ERROR);
    }
  }

  /**
   * 账号密码用户鉴权设置
   *
   * @param restClientBuilder
   */
  private void setAuthConfig(RestClientBuilder restClientBuilder) {
    if (StringUtils.isNoneBlank(serverConfig.getEsUserName()) && StringUtils
        .isNoneBlank(serverConfig.getEsPassword())) {
      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY,
          new UsernamePasswordCredentials(serverConfig.getEsUserName(),
              serverConfig.getEsPassword()));
      restClientBuilder.setHttpClientConfigCallback(asyncClientBuilder -> {
        asyncClientBuilder.disableAuthCaching();
        return asyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      });
    }
  }

  /**
   * 解析clusterNode地址
   *
   * @param clusterNodes
   * @return
   */
  private HttpHost[] loadHttpHosts(String clusterNodes) {
    String[] clusterNodesArray = clusterNodes.split(COMMA);
    HttpHost[] httpHosts = new HttpHost[clusterNodesArray.length];
    for (int i = 0; i < clusterNodesArray.length; i++) {
      String clusterNode = clusterNodesArray[i];
      String[] hostAndPort = clusterNode.split(COLON);
      httpHosts[i] = new HttpHost(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
    }
    return httpHosts;
  }

  /**
   * 配置连接超时时间等参数
   *
   * @param restClientBuilder 创建restClient的构造器
   */
  private void setConnectTimeOutConfig(RestClientBuilder restClientBuilder) {
    restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
      requestConfigBuilder.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
      requestConfigBuilder.setSocketTimeout(SOCKET_TIMEOUT_MILLIS);
      requestConfigBuilder.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLIS);
      return requestConfigBuilder;
    });
  }

  /**
   * 使用异步httpclient时设置并发连接数
   *
   * @param restClientBuilder 创建restClient的构造器
   */
  private void setConnectConfig(RestClientBuilder restClientBuilder) {
    restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
      httpClientBuilder.setMaxConnTotal(MAX_CONN_TOTAL);
      httpClientBuilder.setMaxConnPerRoute(MAX_CONN_PER_ROUTE);
      return httpClientBuilder;
    });
  }

  private static class SingletonHolder {

    private static final EsClientServiceClient INSTANCE = new EsClientServiceClient();
  }

}
