package com.rothsCode.liteGateway.core.model;

import java.io.Serializable;
import lombok.Data;
import org.apache.http.HttpStatus;

/**
 * @author roths
 * @Description:
 * @date 2023/8/17 17:55
 */
@Data
public class Result<T> implements Serializable {

  private static final long serialVersionUID = 4870204647444914775L;

  /**
   * 成功标志
   */
  private boolean success = true;

  /**
   * 返回处理消息
   */
  private String message = "操作成功！";

  /**
   * 返回代码
   */
  private Integer code = 0;

  /**
   * 返回数据对象 data
   */
  private T result;

  /**
   * 时间戳
   */
  private long timestamp = System.currentTimeMillis();

  public Result() {
    this.code = HttpStatus.SC_OK;
  }

  public static Result<?> ok() {
    Result<?> r = new Result<>();
    r.setSuccess(true);
    r.setCode(HttpStatus.SC_OK);
    r.setMessage("操作成功");
    return r;
  }

  public static Result<?> ok(String msg) {
    Result<?> r = new Result<>();
    r.setSuccess(true);
    r.setCode(HttpStatus.SC_OK);
    r.setMessage(msg);
    return r;
  }

  public static <T> Result<T> ok(T data) {
    Result<T> r = new Result<>();
    r.setSuccess(true);
    r.setCode(HttpStatus.SC_OK);
    r.setResult(data);
    return r;
  }

  public static <T> Result<T> error(String msg) {
    return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
  }

  public static <T> Result<T> errorGatewayError(String msg) {
    return error(HttpStatus.SC_BAD_GATEWAY, msg);
  }

  public static <T> Result<T> errorGatewayError() {
    return error(HttpStatus.SC_BAD_GATEWAY, "网关内部错误");
  }

  public static <T> Result<T> errorGatewayTimeOut() {
    return error(HttpStatus.SC_GATEWAY_TIMEOUT, "网关超时");
  }

  public static <T> Result<T> errorMsg401() {
    return error(HttpStatus.SC_UNAUTHORIZED, "请重新登录");
  }

  public static <T> Result<T> error(int code, String msg) {
    Result<T> r = new Result<>();
    r.setCode(code);
    r.setMessage(msg);
    r.setSuccess(false);
    return r;
  }

  public Result<T> error500(String message) {
    this.message = message;
    this.code = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    this.success = false;
    return this;
  }

  public Result<T> error401(String message) {
    this.message = message;
    this.code = HttpStatus.SC_UNAUTHORIZED;
    this.success = false;
    return this;
  }
}
