package com.qcloud.cos.exception;

// 网络异常(如网络故障，导致无法连接服务端)
public class NetworkException extends AbstractCosException {

    private static final long serialVersionUID = -6662661467437143397L;

    public NetworkException(String message) {
        super(CosExceptionType.NETWORK_EXCEPITON, message);
    }

}
