package com.qcloud.cos.request;

/**
 * @author chengwu
 * 删除文件请求
 */
public class AbstractDelRequest extends AbstractBaseRequest {

    public AbstractDelRequest(String bucketName, String cosPath) {
        super(bucketName, cosPath);
    }
    
}
