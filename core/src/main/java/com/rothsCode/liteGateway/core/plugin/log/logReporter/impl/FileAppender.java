package com.rothsCode.liteGateway.core.plugin.log.logReporter.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import lombok.extern.slf4j.Slf4j;

/**
 * @author roths
 * @Description: 文件追加类
 * @date 2023/8/29 21:15
 */
@Slf4j
public class FileAppender {

  private RandomAccessFile rds;
  private File destFile;

  public FileAppender(String storagePath, String fileName) {
    File fileDir = new File(storagePath);
    if (!fileDir.exists()) {
      fileDir.mkdirs();
    }
    destFile = new File(storagePath + "\\" + fileName + ".log");
    try {
      if (!destFile.exists()) {
        boolean createFlag = destFile.createNewFile();
        if (!createFlag) {
          log.error("文件目录生成失败:{}", fileName);
        }
      }
      rds = new RandomAccessFile(destFile, "rw");
      rds.seek(rds.length());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void append(byte[] body) {
    try {
      rds.write(body);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 关闭资源 发送文件消息到nameNode
   */
  public void close() {
    if (rds != null) {
      try {
        rds.close();
      } catch (IOException e) {
        log.error("关闭fileAppender失败:{}", e.getMessage());
        e.printStackTrace();
      }
    }

  }

}
