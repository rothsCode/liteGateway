package com.rothsCode.liteGateway.core.util.radixTree;

import org.apache.commons.lang3.StringUtils;

/**
 * @author roths
 * @Description: 单个ip和cidr段
 * @date 2023/9/30 16:10
 */
public class IPCIDRRadixTree<T> implements IRadixTree<T> {

  private static final String SPLIT_SYMBOL = "/";
  /**
   * cidr前缀树
   */
  private CIDRRadixTree<T> cidrRadixTree = new CIDRRadixTree<>();

  /**
   * 单个ip前缀树
   */
  private TextRadixTree<T> ipRadixTree = new TextRadixTree();

  public static void main(String[] args) throws Exception {
    IPCIDRRadixTree tree = new IPCIDRRadixTree();
    tree.put("192.168.2.0/24");
    tree.put("192.162.23.14");
    System.out.println(tree.search("192.168.2.245"));
    System.out.println(tree.search("192.168.3.3"));
    System.out.println(tree.search("192.162.23.15"));
    tree.put("192.168.2.0/24", "dddd");
    tree.put("192.162.23.14", "rrr");
    System.out.println(tree.findValueByKey("192.168.2.245"));
    System.out.println(tree.findValueByKey("192.168.3.245"));
    System.out.println(tree.findValueByKey("192.162.23.15"));
    System.out.println(tree.findValueByKey("192.162.23.14"));
  }

  @Override
  public void put(String ip, T value) throws Exception {
    //判断是单个ip还是cidr段
    if (StringUtils.isEmpty(ip)) {
      return;
    }
    if (ip.contains(SPLIT_SYMBOL)) {
      cidrRadixTree.put(ip, value);
    } else {
      ipRadixTree.put(ip, value);
    }

  }

  @Override
  public void put(String ip) throws Exception {
    //判断是单个ip还是cidr段
    if (StringUtils.isEmpty(ip)) {
      return;
    }
    if (ip.contains(SPLIT_SYMBOL)) {
      cidrRadixTree.put(ip);
    } else {
      ipRadixTree.put(ip, (T) "");
    }
  }

  @Override
  public boolean search(String ipAddress) {
    if (StringUtils.isEmpty(ipAddress)) {
      return false;
    }
    //优先ip匹配然后cidr匹配
    T value = ipRadixTree.findValueByKey(ipAddress);
    if (value == null) {
      return cidrRadixTree.search(ipAddress);
    }
    return true;
  }

  @Override
  public T findValueByKey(String ipAddress) {
    if (StringUtils.isEmpty(ipAddress)) {
      return null;
    }
    //优先ip匹配然后cidr匹配
    T value = ipRadixTree.findValueByKey(ipAddress);
    if (value == null) {
      value = cidrRadixTree.findValueByKey(ipAddress);
    }
    return value;
  }
}
