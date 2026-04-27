package com.example.springai.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用 API 响应包装类
 * ok=1 表示成功，ok=0 表示失败。
 */
@Data
@NoArgsConstructor
public class Result {
    /** 状态标识：1 成功，0 失败 */
    private Integer ok;
    /** 提示信息 */
    private String msg;

    private Result(Integer ok, String msg) {
        this.ok = ok;
        this.msg = msg;
    }

    /** 构造成功响应 */
    public static Result ok() {
        return new Result(1, "ok");
    }

    /** 构造失败响应 */
    public static Result fail(String msg) {
        return new Result(0, msg);
    }
}