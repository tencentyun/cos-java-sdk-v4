package com.qcloud.cos.exception;

// 参数异常
public class ParamException extends AbstractCosException {

    private static final long serialVersionUID = 216921496331691543L;

    public ParamException(String message) {
        super(CosExceptionType.PARAM_EXCEPTION, message);
    }
    
}
