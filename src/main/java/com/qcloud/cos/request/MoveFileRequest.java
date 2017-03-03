package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;
import com.qcloud.cos.meta.OverWrite;

public class MoveFileRequest extends AbstractBaseRequest {
	private String dstCosPath = "";
	private OverWrite overWrite = OverWrite.NO_OVER_WRITE;
	
	public MoveFileRequest(String bucketName, String cosPath, String dstCosPath) {
		super(bucketName, cosPath);
		this.dstCosPath = dstCosPath;
	}
	
	public String getDstCosPath() {
		return this.dstCosPath;
	}
	
	public void setDstCosPath(String dstCosPath) {
		this.dstCosPath = dstCosPath;
	}
	
	public OverWrite getOverWrite() {                                                               
        return overWrite;                                                                           
    }                                                                                               
                                                                                                    
    public void setOverWrite(OverWrite overWrite) {                                                 
        this.overWrite = overWrite;                                                                 
    }

    @Override
    public void check_param() throws ParamException {
    	super.check_param();
    	CommonParamCheckUtils.AssertLegalCosFilePath(this.getCosPath());                            
        CommonParamCheckUtils.AssertLegalCosFilePath(this.dstCosPath);                              
        CommonParamCheckUtils.AssertNotNull("overWrite", this.overWrite);
    }
    
}
