package com.rothsCode.liteGateway.core.pipeline.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rothscode
 * @Description:
 * @date 2022/8/18 11:45
 */
public class HandlerChainEngineFactory {

  /**
   * 处理链
   */
  private static Map<String, HandlerChainEngine> CHAIN_ENGINE_MAP = new HashMap<>();

  /**
   * 根据事件类型获取对应事件链
   *
   * @param handleType
   * @return
   */
  public static HandlerChainEngine getHandlerChainEngine(String handleType) {
    HandlerChainEngine handlerChainEngine = CHAIN_ENGINE_MAP.get(handleType);
    if (handlerChainEngine == null) {
      handlerChainEngine = new HandlerChainEngine(handleType);
      CHAIN_ENGINE_MAP.put(handleType, handlerChainEngine);
    }
    return handlerChainEngine;

  }

}
