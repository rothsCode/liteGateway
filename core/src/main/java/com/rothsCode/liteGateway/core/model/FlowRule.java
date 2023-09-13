package com.rothsCode.liteGateway.core.model;

import lombok.Data;

/**
 * @author roths
 * @Description: 流控规则 TODO 多维度混合限流
 * @date 2023/8/25 15:28
 */
@Data
public class FlowRule {

  /**
   * 资源名 可以为服务 ip,path，用户等相关维度
   */
  private String resourceName;

  /**
   * 资源名 可以为服务 ip,path，用户等相关维度
   */
  private String resourceValue;
  /**
   * 资源类型 RateLimitResourceTypeEnum
   */
  private String resourceType;

  /**
   * 最大许可请求数
   */
  private int maxPermits;

  /**
   * 最大流量等待阈值，超过则直接拒绝
   */
  private int maxWaitingRequests;

  /**
   * 流量预热时间
   */
  private int warmUpPeriodAsSecond;

  /**
   * 限流类型 内存 redis  默认内存限流 redis 在高流量下存在网络瓶颈,故全局维度不适用于redis 并且redis发生故障则退化为单机内存限流
   */
  private String rateLimitType;


}
