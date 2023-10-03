package com.rothsCode.liteGateway.core.pipeline.rateLimiter;

import cn.hutool.core.collection.CollectionUtil;
import com.rothsCode.liteGateway.core.model.FlowRule;
import com.rothsCode.liteGateway.core.util.radixTree.IPCIDRRadixTree;
import com.rothsCode.liteGateway.core.util.radixTree.TextRadixTree;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roths
 * @Description: 流控控制管理器 针对全局以及单路径资源进行流量管控
 * @date 2023/8/25 15:02
 */
public class FlowControlManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowControlManager.class);


  private static final FlowControlManager INSTANCE = new FlowControlManager();

  private TextRadixTree<FlowControl> urlRadixTree;

  private IPCIDRRadixTree<FlowControl> ipcidrRadixTree;
  /**
   * 全局限流器
   */
  private FlowControl globalFlowControl;


  public static FlowControlManager getInstance() {
    return INSTANCE;
  }

  public FlowControl getGlobalFlowControl() {
    return globalFlowControl;
  }

  public IPCIDRRadixTree<FlowControl> getIpcidrRadixTree() {
    return ipcidrRadixTree;
  }

  public TextRadixTree<FlowControl> getUrlRadixTree() {
    return urlRadixTree;
  }

  /**
   * TODO 存在全量更新问题
   *
   * @param flowRuleList Copy on write
   */
  public synchronized void initFLow(List<FlowRule> flowRuleList) {
    if (CollectionUtil.isEmpty(flowRuleList)) {
      return;
    }
    TextRadixTree<FlowControl> temUrlRadixTree = new TextRadixTree();
    IPCIDRRadixTree<FlowControl> tempIpcidrRadixTree = new IPCIDRRadixTree<>();
    for (FlowRule f : flowRuleList) {
      FlowControl flowControl = new FlowControl(f.getRateLimitType(), f.getResourceValue(),
          f.getMaxPermits(), f.getWarmUpPeriodAsSecond(),
          f.getMaxWaitingRequests());
      if (RateLimitResourceTypeEnum.GLOBAL.getCode().equals(f.getResourceType())) {
        globalFlowControl = flowControl;
      } else if (RateLimitResourceTypeEnum.URL.getCode().equals(f.getResourceType())) {
        temUrlRadixTree.put(f.getResourceValue(), flowControl);
      } else if (RateLimitResourceTypeEnum.IP.getCode().equals(f.getResourceType())) {
        try {
          tempIpcidrRadixTree.put(f.getResourceValue(), flowControl);
        } catch (Exception e) {
          LOGGER.error("ipcidrRadixTree error:{}", e);
        }
      }
    }
    urlRadixTree = temUrlRadixTree;
    ipcidrRadixTree = tempIpcidrRadixTree;
  }

}



