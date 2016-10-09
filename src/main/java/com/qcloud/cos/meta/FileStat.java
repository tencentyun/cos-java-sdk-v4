package com.qcloud.cos.meta;

import java.io.Serializable;

import com.qcloud.cos.common_utils.CommonFileUtils;

/**
 * @author chengwu
 * 文件状态, 包括文件大小，最近修改时间
 */
public class FileStat implements Serializable {

    private static final long serialVersionUID = -4572277446000987482L;

    public FileStat() {}

    public static FileStat getFileStat(String filePath) throws Exception {
        FileStat fileStat = new FileStat();
        fileStat.fileSize = CommonFileUtils.getFileLength(filePath);
        fileStat.lastModifiedTime = CommonFileUtils.getFileLastModified(filePath);
        return fileStat;
    }

    private long fileSize;
    private long lastModifiedTime;

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (((FileStat) obj).getFileSize() == this.fileSize
                && ((FileStat) obj).getLastModifiedTime() == this.lastModifiedTime) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hashValue = 1;
        final int prime = 31;
        hashValue = prime * hashValue + new Long(fileSize).hashCode();
        hashValue = prime * hashValue + new Long(lastModifiedTime).hashCode();
        return hashValue;
    }
}
