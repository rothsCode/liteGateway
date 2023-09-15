package com.rothsCode.liteGateway.core.pipeline.core;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.rothsCode.liteGateway.core.pipeline.enums.HandleParamTypeEnum;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author rothscode
 * @Description: 基于责任链模式的处理链执行器
 * @date 2022/7/30 18:00
 */
public class HandlerChainEngine {

  public static final Logger log = LoggerFactory.getLogger(HandlerChainEngine.class);
  /**
   * 处理事件
   */
  private static List<HandlerEvent> chainList;

  /**
   * 过滤当前类型的事件
   *
   * @param handleType
   */
  public HandlerChainEngine(String handleType) {
    Assert.notEmpty(handleType, "事件类型不可为空");
    ServiceLoader<IHandlerEvent> handleEvents = ServiceLoader.load(IHandlerEvent.class);
    chainList = new ArrayList<>();
    for (IHandlerEvent h : handleEvents) {
      HandlerEvent handlerEvent = (HandlerEvent) h;
      if (ObjectUtil.equal(handleType, handlerEvent.handleEvent().getEventType())) {
        chainList.add(handlerEvent);
      }
    }
    //排序
    chainList = chainList.stream().sorted(Comparator.comparingInt(t -> t.handleEvent().getSort()))
        .collect(Collectors.toList());
    for (int i = 0; i < chainList.size() - 1; i++) {
      HandlerEvent current = chainList.get(i);
      current.addNext(chainList.get(i + 1));
    }

  }

  /**
   * 自定义组合事件链
   */
  public void addHandleEvent(HandlerEvent handlerEvent) {
    if (CollectionUtil.isEmpty(chainList)) {
      chainList = new ArrayList<>(8);
    }
    chainList.add(handlerEvent);
  }

  /**
   * 执行处理事件
   *
   * @param handlerContext
   */
  public static void processEvent(HandlerContext handlerContext) {
    if (chainList.size() > 0) {
      Object handleHead = handlerContext.getObject(HandleParamTypeEnum.HANDLE_HEAD_INDEX.getCode());
      int handleHeadIndex = 0;
      if (handleHead != null) {
        handleHeadIndex = (int) handleHead;
      }
      chainList.get(handleHeadIndex).doHandle(handlerContext);
    }
  }

}



