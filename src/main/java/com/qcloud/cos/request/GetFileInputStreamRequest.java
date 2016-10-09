package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;

/**
 * @author chengwu 获取下载文件的输入流
 */
public class GetFileInputStreamRequest extends AbstractBaseRequest {

    // 使用CDN加速下载，默认开启, 否则从COS源站进行下载
    private boolean useCDN = true;

    // referer设置, 如果没开启referer防盗链，可以不设置
    private String referer = "";

    // 下载文件的range的起始位置
    private long rangeStart = 0;
    // 下载文件的range的结束位置
    private long rangeEnd = Long.MAX_VALUE;

    public GetFileInputStreamRequest(String bucketName, String cosPath) {
        super(bucketName, cosPath);
    }

    @Override
    public void check_param() throws ParamException {
        super.check_param();
        CommonParamCheckUtils.AssertLegalCosFilePath(this.getCosPath());
    }

    public long getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(long rangeStart) {
        this.rangeStart = rangeStart;
    }

    public long getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(long rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public boolean isUseCDN() {
        return useCDN;
    }

    public void setUseCDN(boolean useCDN) {
        this.useCDN = useCDN;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
