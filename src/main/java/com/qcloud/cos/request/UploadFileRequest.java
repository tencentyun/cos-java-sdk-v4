package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;
import com.qcloud.cos.meta.InsertOnly;

/**
 * @author chengwu 上传文件请求,针对文件整体上传，不分片的操作
 */
public class UploadFileRequest extends AbstractBaseRequest {
    // 默认最大并发度，这里是16个线程并发发送
    private static final int DEFAULT_TASK_NUM = 16;
    // 需要上传的路径
    private String localPath;
    // 上传文件的属性信息
    private String bizAttr;

    private byte[] contentBufer = null;
    private boolean uploadFromBuffer = false;

    private InsertOnly insertOnly = InsertOnly.NO_OVER_WRITE;

    // 开启sha摘要
    protected boolean enableShaDigest = false;

    // 并行任务数
    protected int taskNum = DEFAULT_TASK_NUM;

    public UploadFileRequest(String bucketName, String cosPath, String localPath, String bizAttr) {
        super(bucketName, cosPath);
        this.localPath = localPath;
        this.bizAttr = bizAttr;
        this.contentBufer = null;
        this.uploadFromBuffer = false;
    }

    public UploadFileRequest(String bucketName, String cosPath, String localPath) {
        this(bucketName, cosPath, localPath, "");
    }

    public UploadFileRequest(String bucketName, String cosPath, byte[] contentBuffer) {
        super(bucketName, cosPath);
        this.contentBufer = contentBuffer;
        this.uploadFromBuffer = true;
        this.bizAttr = "";
    }

    public String getBizAttr() {
        return bizAttr;
    }

    public void setBizAttr(String bizAttr) {
        this.bizAttr = bizAttr;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
        this.uploadFromBuffer = false;
    }

    public InsertOnly getInsertOnly() {
        return insertOnly;
    }

    public void setInsertOnly(InsertOnly insertOnly) {
        this.insertOnly = insertOnly;
    }

    public byte[] getContentBufer() {
        return contentBufer;
    }

    public void setContentBufer(byte[] contentBufer) {
        this.contentBufer = contentBufer;
        this.uploadFromBuffer = true;
    }

    public boolean isUploadFromBuffer() {
        return uploadFromBuffer;
    }


    @Override
    public void check_param() throws ParamException {
        super.check_param();
        CommonParamCheckUtils.AssertLegalCosFilePath(this.getCosPath());
        CommonParamCheckUtils.AssertNotNull("biz_attr", this.bizAttr);
        CommonParamCheckUtils.AssertNotNull("insertOnly", this.insertOnly);
        if (!this.uploadFromBuffer) {
            CommonParamCheckUtils.AssertLegalLocalFilePath(this.localPath);
        } else {
            CommonParamCheckUtils.AssertNotNull("contentBufer", contentBufer);
        }
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    public boolean isEnableShaDigest() {
        return enableShaDigest;
    }

    public void setEnableShaDigest(boolean enableShaDigest) {
        this.enableShaDigest = enableShaDigest;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", local_path:").append(getMemberStringValue(this.localPath));
        sb.append(", bizAttr:").append(getMemberStringValue(this.bizAttr));
        sb.append(", uploadFromBuffer:").append(this.uploadFromBuffer);
        sb.append(", insertonly:");
        if (this.insertOnly == null) {
            sb.append("null");
        } else {
            sb.append(this.insertOnly.ordinal());
        }
        return sb.toString();
    }
}
