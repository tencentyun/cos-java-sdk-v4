package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;

/**
 * @author chengwu 更新目录请求
 *
 */
public class UpdateFolderRequest extends AbstractBaseRequest {

	// biz_attr属性
	private String bizAttr = "";

	public UpdateFolderRequest(String bucketName, String cosPath) {
		super(bucketName, cosPath);
	}

	public String getBizAttr() {
		return bizAttr;
	}

	public void setBizAttr(String bizAttr) {
		this.bizAttr = bizAttr;
	}

	@Override
	public void check_param() throws ParamException {
		super.check_param();
		CommonParamCheckUtils.AssertLegalCosFolderPath(this.getCosPath());
		CommonParamCheckUtils.AssertNotRootCosPath(this.getCosPath());
		CommonParamCheckUtils.AssertNotNull("biz_attr", this.bizAttr);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(", biz_attr:").append(getMemberStringValue(bizAttr));
		return sb.toString();
	}
}
