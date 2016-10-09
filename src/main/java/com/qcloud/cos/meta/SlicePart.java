package com.qcloud.cos.meta;

import java.io.Serializable;

/**
 * @author chengwu
 * 分片信息，包括偏移量，分片大小，是否上传成功
 */
public class SlicePart implements Serializable {
    
    private static final long serialVersionUID = -7454131654081550885L;
    
    private long offset;
    private int sliceSize;
    private boolean uploadCompleted = false;

    public SlicePart() {}

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getSliceSize() {
        return sliceSize;
    }

    public void setSliceSize(int sliceSize) {
        this.sliceSize = sliceSize;
    }

    public boolean isUploadCompleted() {
        return uploadCompleted;
    }

    public void setUploadCompleted(boolean uploadCompleted) {
        this.uploadCompleted = uploadCompleted;
    }
    
    @Override
    public int hashCode() {
        int hashValue = 1;
        final int prime = 31;
        hashValue = prime * hashValue + new Long(offset).hashCode();
        hashValue = prime * hashValue + new Integer(sliceSize).hashCode();
        hashValue = prime * hashValue + new Boolean(uploadCompleted).hashCode();
        return hashValue;
    }

}
