package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;

public class ListPartsRequest extends AbstractBaseRequest {

    public ListPartsRequest(String bucketName, String cosPath) {
        super(bucketName, cosPath);
    }

    @Override
    public void check_param() throws ParamException {
        super.check_param();
        CommonParamCheckUtils.AssertLegalCosFilePath(this.getCosPath());
    }

}
