package com.qcloud.cos.meta;

import com.qcloud.cos.request.UploadSliceFileRequest;

/**
 * 分片上传的上下文信息, 用于在多个步骤之间传递
 * 
 * @author chengwu
 *
 */
public class UploadSliceFileContext {
    // bucket名
    private String bucketName = "";
    // cos路径
    private String cosPath = "";
    // 本地文件路径
    private String localPath = "";
    // 文件bizAttr
    private String bizAttr = "";
    // 是否覆盖
    private InsertOnly insertOnly = InsertOnly.NO_OVER_WRITE;
    private int sliceSize;
    // 文件大小
    private long fileSize = 0;

    private String url = "";
    // 开启sha摘要
    private boolean enableShaDigest = true;
    // 开启断点请求，默认开启
    private boolean enableSavePoint = true;
    // 存储的端点文件路径
    private String savePointFile = null;
    // 任务数量
    private int taskNum = 1;
    // session
    private String sessionId = "";
    // 全文sha
    private String entireFileSha = "";

    private boolean serialUpload = true;

    private boolean uploadFromBuffer = false;
    private byte[] contentBuffer = null;

    public UploadSliceFileContext(UploadSliceFileRequest request) {
        this.bucketName = request.getBucketName();
        this.cosPath = request.getCosPath();
        this.uploadFromBuffer = request.isUploadFromBuffer();
        if (request.isUploadFromBuffer()) {
            this.contentBuffer = request.getContentBufer();
        }
        this.localPath = request.getLocalPath();
        this.bizAttr = request.getBizAttr();
        this.insertOnly = request.getInsertOnly();
        this.sliceSize = request.getSliceSize();
        this.taskNum = request.getTaskNum();
        this.enableSavePoint = request.isEnableSavePoint();
        this.enableShaDigest = request.isEnableShaDigest();
        this.savePointFile = this.localPath + ".scp";
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getCosPath() {
        return cosPath;
    }

    public void setCosPath(String cosPath) {
        this.cosPath = cosPath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getBizAttr() {
        return bizAttr;
    }

    public void setBizAttr(String bizAttr) {
        this.bizAttr = bizAttr;
    }

    public InsertOnly getInsertOnly() {
        return insertOnly;
    }

    public void setInsertOnly(InsertOnly insertOnly) {
        this.insertOnly = insertOnly;
    }

    public int getSliceSize() {
        return sliceSize;
    }

    public void setSliceSize(int sliceSize) {
        this.sliceSize = sliceSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnableShaDigest() {
        return enableShaDigest;
    }

    public void setEnableShaDigest(boolean enableShaDigest) {
        this.enableShaDigest = enableShaDigest;
    }

    public boolean isEnableSavePoint() {
        return enableSavePoint;
    }

    public void setEnableSavePoint(boolean enableSavePoint) {
        this.enableSavePoint = enableSavePoint;
    }

    public String getSavePointFile() {
        return savePointFile;
    }

    public void setSavePointFile(String savePointFile) {
        this.savePointFile = savePointFile;
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getEntireFileSha() {
        return entireFileSha;
    }

    public void setEntireFileSha(String entireFileSha) {
        this.entireFileSha = entireFileSha;
    }

    public boolean isSerialUpload() {
        return serialUpload;
    }

    public void setSerialUpload(boolean serialUpload) {
        this.serialUpload = serialUpload;
    }

    public boolean isUploadFromBuffer() {
        return uploadFromBuffer;
    }

    public void setUploadFromBuffer(boolean uploadFromBuffer) {
        this.uploadFromBuffer = uploadFromBuffer;
    }

    public byte[] getContentBuffer() {
        return contentBuffer;
    }

    public void setContentBuffer(byte[] contentBuffer) {
        this.contentBuffer = contentBuffer;
    }

}
