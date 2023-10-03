package com.rothsCode.liteGateway.core.util.radixTree;

/**
 * @author roths
 * @Description:
 * @date 2023/9/30 16:19
 */
public interface IRadixTree<T> {

  void put(String key, T value) throws Exception;

  /**
   * 针对无value情况
   *
   * @param key
   * @throws Exception
   */
  void put(String key) throws Exception;

  /**
   * 针对无value情况
   *
   * @param key
   * @return boolean
   * @throws Exception
   */
  boolean search(String key);

  /**
   * 针对有value情况
   *
   * @param key
   * @return T
   * @throws Exception
   */
  T findValueByKey(String key);

}
