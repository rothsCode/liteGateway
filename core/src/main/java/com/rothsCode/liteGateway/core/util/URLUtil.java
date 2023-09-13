package com.rothsCode.liteGateway.core.util;

import cn.hutool.core.text.AntPathMatcher;

/**
 * @author roths
 * @Description: url工具类
 * @date 2023/8/28 16:13
 */
public class URLUtil {

  private static final AntPathMatcher pathMatcher = new AntPathMatcher();


  public static boolean matchURL(String patten, String path) {
    return pathMatcher.match(patten, path);
  }

}
