package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;

/**
 * @author chengwu
 * 获取目录属性信息
 */
public class StatFolderRequest extends AbstractStatRequest {

    public StatFolderRequest(String bucketName, String cosPath) {
        super(bucketName, cosPath);
    }
    
    @Override
    public void check_param() throws ParamException {
    	super.check_param();
    	CommonParamCheckUtils.AssertLegalCosFolderPath(this.getCosPath());
    }

}
