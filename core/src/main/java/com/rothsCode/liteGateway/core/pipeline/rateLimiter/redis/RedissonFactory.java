package com.rothsCode.liteGateway.core.pipeline.rateLimiter.redis;

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @author roths
 * @Description:
 * @date 2023/8/28 19:47
 */
public class RedissonFactory {

  public static RedissonClient redissonClient() {
    Config config = new Config();
    ServerConfig serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    if ("standAlone".equals(serverConfig.getRedisModel())) {
      config.useSingleServer().setAddress("redis://" + serverConfig.getRedisAddress())
          .setConnectionMinimumIdleSize(10)
          .setConnectTimeout(3000).setDatabase(0);
      if (StringUtils.isNotBlank(serverConfig.getRedisPassword())) {
        config.useSingleServer().setPassword(serverConfig.getRedisPassword());
      }
    } else if ("sentinel".equals(serverConfig.getRedisModel())) {
      List<String> nodeAddressList = Arrays.asList(serverConfig.getRedisAddress().split(","));
      for (String nodeAddress : nodeAddressList) {
        config.useReplicatedServers().addNodeAddress("redis://" + nodeAddress);
      }
    } else if ("cluster".equals(serverConfig.getRedisModel())) {
      List<String> nodeAddressList = Arrays.asList(serverConfig.getRedisAddress().split(","));
      for (String nodeAddress : nodeAddressList) {
        config.useClusterServers().addNodeAddress("redis://" + nodeAddress);
      }
    }
    RedissonClient redissonClient = Redisson.create(config);
    return redissonClient;
  }


}
