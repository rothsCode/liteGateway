package com.rothsCode.liteGateway.core.util.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

/**
 * @author roths
 * @Description: 缓存管理器
 * @date 2023/8/17 11:00
 */
public class CacheManager {

  private static Cache<String, String> CACHE = Caffeine.newBuilder()
      .maximumSize(1024)
      .expireAfterWrite(5, TimeUnit.MINUTES)
      .build();

  public static void putCache(String cacheKey, String cache) {
    CACHE.put(cacheKey, cache);
  }

  public static String getCache(String cacheKey) {
    return CACHE.getIfPresent(cacheKey);
  }


}
