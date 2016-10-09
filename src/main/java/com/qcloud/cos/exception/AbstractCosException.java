package com.qcloud.cos.exception;

import org.json.JSONObject;

import com.qcloud.cos.http.ResponseBodyKey;

/**
 * 封装cos异常
 * @author chengwu
 *
 */
public abstract class AbstractCosException extends Exception {

    private static final long serialVersionUID = 7547532865194837136L;
    
    private CosExceptionType type;

    public AbstractCosException(CosExceptionType type, String message) {
        super(message);
        this.type = type;
    }

    public CosExceptionType getType() {
        return type;
    }
    
    @Override
    public String toString() {
        JSONObject responseObj = new JSONObject();
        responseObj.put(ResponseBodyKey.CODE, type.getErrorCode());
        responseObj.put(ResponseBodyKey.MESSAGE, getMessage());
        return responseObj.toString();
    }
    
}
