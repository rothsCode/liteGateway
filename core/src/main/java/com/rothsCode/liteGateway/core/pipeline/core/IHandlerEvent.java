package com.rothsCode.liteGateway.core.pipeline.core;

import com.rothsCode.liteGateway.core.pipeline.enums.HandleEventEnum;

/**
 * @author rothscode
 * @Description:
 * @date 2022/7/30 18:12
 */
public interface IHandlerEvent<T> {


  /**
   * 事件种类便于区分执行
   */
  HandleEventEnum handleEvent();

  /**
   * 执行方法
   */
  void doHandle(T t) throws Exception;

}
