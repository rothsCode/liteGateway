package com.rothsCode.liteGateway.core.util.radixTree;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author roths
 * @Description: cidr段
 * @date 2023/9/30 15:29
 */
public class CIDRRadixTree<T> implements IRadixTree<T> {

  private static final String SPLIT_SYMBOL = "/";

  private Node root;

  public CIDRRadixTree() {
    root = new Node();
  }

  public static void main(String[] args) throws Exception {
    CIDRRadixTree tree = new CIDRRadixTree();
    // tree.insert("192.168.0.0/16");
    tree.insert("192.168.2.0/24");
    System.out.println(tree.search("192.168.2.245")); // true
    System.out.println(tree.search("192.168.3.3")); // false
    CIDRRadixTree<String> valueRadixTree = new CIDRRadixTree();
    valueRadixTree.put("192.168.2.0/24", "dddd");
    System.out.println(valueRadixTree.findValueByKey("192.168.2.245"));
    System.out.println(valueRadixTree.findValueByKey("192.168.3.245")); // true
  }

  /**
   * 针对无value情况
   *
   * @param cidr
   * @throws Exception
   */
  public void insert(String cidr) throws Exception {
    insertNode(cidr);
  }

  private Node insertNode(String cidr) throws UnknownHostException {
    String[] parts = cidr.split(SPLIT_SYMBOL);
    String ipAddress = parts[0];
    int prefixLength = Integer.parseInt(parts[1]);
    byte[] ipBytes = InetAddress.getByName(ipAddress).getAddress();

    Node node = root;
    for (int i = 0; i < prefixLength; i++) {
      int bit = (ipBytes[i / 8] >> (7 - i % 8)) & 1;
      if (node.children[bit] == null) {
        node.children[bit] = new Node();
      }
      node = node.children[bit];
    }
    node.isLeaf = true;
    return node;
  }

  @Override
  public void put(String cidr, T value) throws Exception {
    Node node = insertNode(cidr);
    node.value = value;
  }

  @Override
  public void put(String cidr) throws Exception {
    insertNode(cidr);
  }

  /**
   * 针对无value情况
   *
   * @param ipAddress
   * @return
   * @throws Exception
   */
  public boolean search(String ipAddress) {
    Node node = null;
    try {
      node = getNode(ipAddress);
    } catch (Exception e) {
      return false;
    }
    if (!node.isLeaf) {
      return false;
    }
    return true;
  }

  private Node getNode(String ipAddress) throws UnknownHostException {
    byte[] ipBytes = InetAddress.getByName(ipAddress).getAddress();
    Node node = root;
    for (int i = 0; i < (ipBytes.length * 8); i++) {
      int bit = (ipBytes[i / 8] >> (7 - i % 8)) & 1;
      if (node.children[bit] == null) {
        break;
      }
      node = node.children[bit];
    }
    return node;
  }

  /**
   * 针对有value情况
   *
   * @param ipAddress
   * @return
   * @throws Exception
   */
  public T findValueByKey(String ipAddress) {
    Node node = null;
    try {
      node = getNode(ipAddress);
    } catch (Exception e) {
      return null;
    }
    if (!node.isLeaf) {
      return null;
    }
    return (T) node.value;
  }

  private static class Node<T> {

    private Node[] children;

    private boolean isLeaf;//是否是叶子节点

    private T value;

    public Node() {
      children = new Node[2];
      isLeaf = false;
    }

  }

}
