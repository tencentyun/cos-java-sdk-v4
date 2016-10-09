package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;

/**
 * @author chengwu 下载文件到本地请求
 */
public class GetFileLocalRequest extends GetFileInputStreamRequest {

	// 要下载到的本地文件
	private String localPath = null;

	public GetFileLocalRequest(String bucketName, String cosPath, String localPath) {
		super(bucketName, cosPath);
		this.localPath = localPath;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	
	
    @Override
	public void check_param() throws ParamException {
		super.check_param();
		CommonParamCheckUtils.AssertLegalCosFilePath(this.getCosPath());
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
