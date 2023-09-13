package com.rothsCode.liteGateway.core.pipeline.rateLimiter.redis;

import com.rothsCode.liteGateway.core.pipeline.rateLimiter.IRateLimiter;
import com.rothsCode.liteGateway.core.pipeline.rateLimiter.RateLimitTypeEnum;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

/**
 * @author roths
 * @Description:
 * @date 2023/8/28 17:24
 */
public class RedisRateLimiter implements IRateLimiter {

  private static final String RATE_LIMIT_PREFIX = "rate_limit";
  private RedissonClient redissonClient;
  private RRateLimiter rateLimiter;

  public RedisRateLimiter(int maxPermits, String resourceValue) {
    redissonClient = RedissonFactory.redissonClient();
    rateLimiter = redissonClient.getRateLimiter(RATE_LIMIT_PREFIX + resourceValue);
    rateLimiter.trySetRate(RateType.OVERALL, maxPermits, 1, RateIntervalUnit.SECONDS);
  }

  @Override
  public String supportType() {
    return RateLimitTypeEnum.REDIS.getCode();
  }

  @Override
  public boolean acquire(int permits) {
    rateLimiter.acquire(permits);
    return true;
  }

  @Override
  public boolean tryAcquire(int permits) {
    return rateLimiter.tryAcquire(permits);
  }
}
