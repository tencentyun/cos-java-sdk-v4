package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;

/**
 * @author chengwu
 * 创建目录请求
 */
public class CreateFolderRequest extends AbstractBaseRequest {
	// 目录属性,默认为空
    private String bizAttr = ""; 

    public CreateFolderRequest(String bucketName, String cosPath) {
        this(bucketName, cosPath, "");
    }
    
    public CreateFolderRequest(String bucketName, String cosPath, String bizAttr) {
        super(bucketName, cosPath);
        this.bizAttr = bizAttr;
    }
    
    public void setBizAttr(String bizAttr) {
    	this.bizAttr = bizAttr;
    }

    public String getBizAttr() {
        return bizAttr;
    }
    
    @Override
    public void check_param() throws ParamException {
    	super.check_param();
    	CommonParamCheckUtils.AssertNotNull("bizAttr", this.bizAttr);
    	CommonParamCheckUtils.AssertLegalCosFolderPath(this.getCosPath());
    	CommonParamCheckUtils.AssertNotRootCosPath(this.getCosPath());
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(super.toString());
    	sb.append(", bizAttr:").append(getMemberStringValue(bizAttr));
    	return sb.toString();
    }
}
