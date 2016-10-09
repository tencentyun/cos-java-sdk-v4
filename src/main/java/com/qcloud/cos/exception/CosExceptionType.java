package com.qcloud.cos.exception;

import com.qcloud.cos.ErrorCode;

// 枚举cos异常类型
public enum CosExceptionType {
    // 参数异常
    PARAM_EXCEPTION(ErrorCode.PARAMS_ERROR, "param_excepiton"),
    // 网络异常
    NETWORK_EXCEPITON(ErrorCode.NETWORK_ERROR, "network_excepiton"),
    // 服务端异常
    SERVER_EXCEPTION(ErrorCode.SERVER_ERROR, "server_exception"),
    // 其他未知异常
    UNKNOWN_EXCEPTION(ErrorCode.UNKNOWN_ERROR, "unknown_exception");

    private int errorCode;
    private String exceptionStr;

    private CosExceptionType(int errorCode, String exceptionStr) {
        this.errorCode = errorCode;
        this.exceptionStr = exceptionStr;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getExceptionStr() {
        return exceptionStr;
    }
    
}
