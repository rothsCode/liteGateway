package com.rothsCode.liteGateway.core.pipeline.rateLimiter;

import com.rothsCode.liteGateway.core.pipeline.rateLimiter.redis.RedisRateLimiter;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description: 流控控制器
 * @date 2023/8/25 15:02
 */
@Slf4j
public class FlowControl {

  private IRateLimiter rateLimiter;


  /**
   * 最大流量等待阈值，超过则直接拒绝
   */
  private int maxWaitingRequests;


  private String resourceValue;


  private AtomicInteger waitingRequests = new AtomicInteger(0);

  public FlowControl(String rateLimitType, String resourceValue, int maxPermits,
      int warmUpPeriodAsSecond,
      int maxWaitingRequests) {
    this.resourceValue = resourceValue;
    this.maxWaitingRequests = maxWaitingRequests;
    if (RateLimitTypeEnum.REDIS.getCode().equals(rateLimitType)) {
      rateLimiter = new RedisRateLimiter(maxPermits, resourceValue);
    } else {
      rateLimiter = new MemoryRateLimiter(maxPermits, warmUpPeriodAsSecond);
    }

  }


  public boolean acquire() {
    return acquire(1);
  }

  public boolean acquire(int permits) {
    //超过最大等待队列阈值则直接拒绝
    if (isOverMaxWaitRequest()) {
      log.info("{}waitingRequestsOver:{}", resourceValue, waitingRequests.get());
      return false;
    }
    boolean success = rateLimiter.tryAcquire(permits);
    if (success) {
      rateLimiter.acquire(permits);
      return true;
    }
    waitingRequests.getAndAdd(permits);
    rateLimiter.acquire(permits);
    waitingRequests.getAndAdd(-permits);
    return true;
  }

  /**
   * 是否超过最大等待阈值
   *
   * @return
   */
  public boolean isOverMaxWaitRequest() {
    return waitingRequests.get() > maxWaitingRequests;
  }

}



