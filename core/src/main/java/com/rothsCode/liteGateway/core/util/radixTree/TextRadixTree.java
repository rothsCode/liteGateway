package com.rothsCode.liteGateway.core.util.radixTree;

import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/**
 * @author roths
 * @Description: 常规字符匹配压缩前缀树 模糊匹配适用于url /**后缀
 * @date 2023/9/28 9:31
 */

public class TextRadixTree<T> implements IRadixTree<T> {

  private static final int NO_MISMATCH = -1;

  private static final String END_SYMBOL = "/**";
  private Node<T> root;

  public TextRadixTree() {
    root = new Node(false);
  }

  public static void main(String[] args) {

    TextRadixTree<String> tree = new TextRadixTree();
    tree.put("/crm-mdm/**", "ddd");
    tree.put("/crm-dms/**", "rtyy");
    tree.put("/crm-dmr/fff", "34y");
    tree.put("/crm-dmr/fff/dddd", "34y");
    String errorValue = tree.findValueByKey("/wrktest/testGet");
    String value = tree.findValueByKey("/crm-mdm/der");
    String value1 = tree.findValueByKey("/crm-dms/ff1/fff/gg/gg");
    String value2 = tree.findValueByKey("/crm-dmr/fff/dddd/44");
    tree.delete("test");
    System.out.println(tree.search("te"));

  }

  public Node insert(String word) {
    Node current = root;
    int currIndex = 0;

    //Iterative approach
    while (currIndex < word.length()) {
      char transitionChar = word.charAt(currIndex);
      Edge currentEdge = current.getTransition(transitionChar);
      //Updated version of the input word
      String currStr = word.substring(currIndex);

      //There is no associated edge with the first character of the current string
      //so simply add the rest of the string and finish
      if (currentEdge == null) {
        current.edges.put(transitionChar, new Edge(currStr));
        break;
      }

      int splitIndex = getFirstMismatchLetter(currStr, currentEdge.label);
      if (splitIndex == NO_MISMATCH) {
        //The edge and leftover string are the same length
        //so finish and update the next node as a word node
        if (currStr.length() == currentEdge.label.length()) {
          currentEdge.next.isLeaf = true;
          break;
        } else if (currStr.length() < currentEdge.label.length()) {
          //The leftover word is a prefix to the edge string, so split
          String suffix = currentEdge.label.substring(currStr.length());
          currentEdge.label = currStr;
          Node newNext = new Node(true);
          Node afterNewNext = currentEdge.next;
          currentEdge.next = newNext;
          newNext.addEdge(suffix, afterNewNext);
          break;
        } else { //currStr.length() > currentEdge.label.length()
          //There is leftover string after a perfect match
          splitIndex = currentEdge.label.length();
        }
      } else {
        //The leftover string and edge string differed, so split at point
        String suffix = currentEdge.label.substring(splitIndex);
        currentEdge.label = currentEdge.label.substring(0, splitIndex);
        Node prevNext = currentEdge.next;
        currentEdge.next = new Node(false);
        currentEdge.next.addEdge(suffix, prevNext);
      }

      //Traverse the tree
      current = currentEdge.next;
      currIndex += splitIndex;
    }
    return current;
  }

  @Override
  public void put(String key, T value) {
    insert(key);
    Node node = getNode(key);
    if (node != null && node.isLeaf) {
      node.value = value;
    }
  }

  @Override
  public void put(String key) {
    insert(key);
  }

  public boolean search(String word) {
    Node node = getNode(word);
    if (node == null) {
      return false;
    }
    return node.isLeaf;
  }

  private Node getNode(String key) {
    Node current = root;
    int currIndex = 0;
    while (currIndex < key.length()) {
      char transitionChar = key.charAt(currIndex);
      Edge edge = current.getTransition(transitionChar);
      if (edge == null) {
        return null;
      }

      String currSubstring = key.substring(currIndex);
      if (edge.label.endsWith(END_SYMBOL)) {
        String prefixLabel = StringUtils.substringBefore(edge.label, END_SYMBOL);
        if (currSubstring.startsWith(prefixLabel)) {
          return edge.next;
        }
      } else if (!currSubstring.startsWith(edge.label)) {
        return null;
      }
      currIndex += edge.label.length();
      current = edge.next;
    }
    return current;
  }

  @Override
  public T findValueByKey(String key) {
    Node node = getNode(key);
    if (node == null || !node.isLeaf) {
      return null;
    }
    return (T) node.value;
  }

  public void delete(String word) {
    root = delete(root, word);
  }

  private Node delete(Node current, String word) {
    //base case, all the characters have been matched from previous checks
    if (word.isEmpty()) {
      //Has no other edges,
      if (current.edges.isEmpty() && current != root) {
        return null;
      }
      current.isLeaf = false;
      return current;
    }

    char transitionChar = word.charAt(0);
    Edge edge = current.getTransition(transitionChar);
    //Has no edge for the current word or the word doesn't exist
    if (edge == null || !word.startsWith(edge.label)) {
      return current;
    }

    Node deleted = delete(edge.next, word.substring(edge.label.length()));
    if (deleted == null) {
      current.edges.remove(transitionChar);
      if (current.totalEdges() == 0 && !current.isLeaf && current != root) {
        return null;
      }
    } else if (deleted.totalEdges() == 1 && !deleted.isLeaf) {
      current.edges.remove(transitionChar);
      for (Object value : deleted.edges.values()) {
        Edge afterDeleted = (Edge) value;
        current.addEdge(edge.label + afterDeleted.label, afterDeleted.next);
      }
    }
    return current;
  }

  private int getFirstMismatchLetter(String word, String edgeWord) {
    int LENGTH = Math.min(word.length(), edgeWord.length());
    for (int i = 1; i < LENGTH; i++) {
      if (word.charAt(i) != edgeWord.charAt(i)) {
        return i;
      }
    }
    return NO_MISMATCH;
  }

  private class Node<T> {

    private boolean isLeaf;
    private HashMap<Character, Edge> edges;
    private T value;

    public Node(boolean isLeaf) {
      this.isLeaf = isLeaf;
      edges = new HashMap<>();
    }

    public Edge getTransition(char transitionChar) {
      return edges.get(transitionChar);
    }

    public void addEdge(String label, Node next) {
      edges.put(label.charAt(0), new Edge(label, next));
    }

    public int totalEdges() {
      return edges.size();
    }
  }

  private class Edge {

    private String label;
    private Node<T> next;

    public Edge(String label) {
      this(label, new Node(true));
    }

    public Edge(String label, Node next) {
      this.label = label;
      this.next = next;
    }
  }

}


