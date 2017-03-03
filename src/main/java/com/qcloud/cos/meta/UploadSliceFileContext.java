package com.qcloud.cos.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qcloud.cos.common_utils.CommonFileUtils;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.UnknownException;
import com.qcloud.cos.http.ResponseBodyKey;
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
    // 分片大小
    private int sliceSize;
    // 文件大小
    private long fileSize = 0;

    private String url = "";
    // 开启sha摘要
    private boolean enableShaDigest = true;
    // 任务数量
    private int taskNum = 1;
    // session
    private String sessionId = "";
    // 全文sha
    private String entireFileSha = "";

    private boolean serialUpload = true;

    // 标识是否是从内存中上传文件
    private boolean uploadFromBuffer = false;
    // 上传的buffer
    private byte[] contentBuffer = null;

    // 标识要全部的slice part，包含已经上传和为上传的，已经上传的slice part的完成属性会设置为true
    public ArrayList<SlicePart> sliceParts;
    // 标识已经上传的slice parts
    private Set<Long> uploadCompletePartsSet;

    public UploadSliceFileContext(UploadSliceFileRequest request) throws AbstractCosException {
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
        this.enableShaDigest = request.isEnableShaDigest();
        this.sliceParts = new ArrayList<SlicePart>();
        this.uploadCompletePartsSet = new HashSet<>();
        caculateFileSize();
    }

    private void caculateFileSize() throws UnknownException {
        try {
            if (this.uploadFromBuffer) {
                this.fileSize = this.contentBuffer.length;
            } else {
                this.fileSize = CommonFileUtils.getFileLength(this.localPath);
            }
        } catch (Exception e) {
            throw new UnknownException("caculateFileSize error. " + e.toString());
        }
    }

    // 切分文件
    public ArrayList<SlicePart> prepareUploadPartsInfo() {
        int sliceCount = new Long((fileSize + (sliceSize - 1)) / sliceSize).intValue();
        for (int sliceIndex = 0; sliceIndex < sliceCount; ++sliceIndex) {
            SlicePart part = new SlicePart();
            long offset = (Long.valueOf(sliceIndex).longValue()) * sliceSize;
            part.setOffset(offset);
            if (sliceIndex != sliceCount - 1) {
                part.setSliceSize(sliceSize);
            } else {
                part.setSliceSize(new Long(fileSize - offset).intValue());
            }
            part.setUploadCompleted(uploadCompletePartsSet.contains(offset));
            sliceParts.add(part);
        }
        return sliceParts;
    }


    public void setUploadCompleteParts(JSONArray listPartsArry) {
        int listPartsLen = listPartsArry.length();
        for (int listPartsIndex = 0; listPartsIndex < listPartsLen; ++listPartsIndex) {
            JSONObject listPartMember = listPartsArry.getJSONObject(listPartsIndex);
            long partOffset = listPartMember.getLong(ResponseBodyKey.Data.OFFSET);
            this.uploadCompletePartsSet.add(partOffset);
        }
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
