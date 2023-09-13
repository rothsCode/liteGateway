package com.rothsCode.liteGateway.core.pipeline.rateLimiter;

/**
 * @author roths
 * @Description: 限流类基类
 * @date 2023/8/28 17:21
 */
public interface IRateLimiter {

  /**
   * 支持类型
   *
   * @return
   */
  String supportType();


  /**
   * 获取资源
   *
   * @return
   */
  boolean acquire(int permits);

  boolean tryAcquire(int permits);

}
