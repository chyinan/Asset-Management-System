package com.project.asset.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "success"),
    BAD_REQUEST(4000, "请求参数错误"),
    VALIDATION_FAILED(4001, "参数校验失败"),
    UNAUTHORIZED(4010, "未认证或凭证已失效"),
    FORBIDDEN(4030, "没有访问权限"),
    NOT_FOUND(4040, "资源不存在"),
    CONFLICT(4090, "资源冲突"),
    SERVER_ERROR(5000, "系统内部异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}


