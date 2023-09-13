package com.rothsCode.liteGateway.core.pipeline.rateLimiter;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.TimeUnit;

/**
 * @author roths
 * @Description:
 * @date 2023/8/28 20:32
 */
public class MemoryRateLimiter implements IRateLimiter {

  private final RateLimiter rateLimiter;

  public MemoryRateLimiter(int maxPermits, long warmUpPeriodAsSecond) {
    if (warmUpPeriodAsSecond == 0) {
      rateLimiter = RateLimiter.create(maxPermits);
    } else {
      rateLimiter = RateLimiter.create(maxPermits, warmUpPeriodAsSecond, TimeUnit.SECONDS);
    }
  }

  @Override
  public String supportType() {
    return RateLimitTypeEnum.MEMORY.getCode();
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
