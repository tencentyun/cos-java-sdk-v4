package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;
/**
 * @author chengwu
 * 封装了请求包含的基本元素
 */
public abstract class AbstractBaseRequest {
    // bucket名
    private String bucketName;
    // cos路径
    private String cosPath;

    public AbstractBaseRequest(String bucketName, String cosPath) {
        super();
        this.bucketName = bucketName;
        this.cosPath = cosPath;
    }

    // 获取bucket名
    public String getBucketName() {
        return bucketName;
    }

    // 设置bucket名
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    // 获取cos_path
    public String getCosPath() {
        return cosPath;
    }

    // 设置cos_path
    public void setCosPath(String cosPath) {
        this.cosPath = cosPath;
    }
    
    protected String getMemberStringValue(String member) {
    	if (member == null) {
    		return "null";
    	} else {
    		return member;
    	}
    }

    // 将request转换为字符串, 用于记录信息
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("bucketName:").append(getMemberStringValue(bucketName));
        sb.append(", cosPath:").append(getMemberStringValue(cosPath));
        return sb.toString();
    }
    

    
    // 检查用户的输入参数
    public void check_param() throws ParamException {
    	CommonParamCheckUtils.AssertNotNull("bucketName", this.bucketName);
    	CommonParamCheckUtils.AssertNotNull("cosPath", this.cosPath);
    }
}
