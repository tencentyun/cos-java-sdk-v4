package com.qcloud.cos.exception;

// 服务端异常(如返回404deng)
public class ServerException extends AbstractCosException {

    private static final long serialVersionUID = -4536038808919814914L;

    public ServerException(String message) {
        super(CosExceptionType.SERVER_EXCEPTION, message);
    }

}
