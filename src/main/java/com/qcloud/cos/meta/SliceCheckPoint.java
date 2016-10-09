package com.qcloud.cos.meta;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.qcloud.cos.common_utils.CommonFileUtils;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.UnknownException;

/**
 * @author chengwu 记录断点信息
 */
public class SliceCheckPoint implements Serializable {

	private static final long serialVersionUID = -5759346251314490168L;

	public static final String DEFAULT_MAGIC = "B61BAAF89E3FD039F1279C4440AD8A7F0250300E";

	public SliceCheckPoint() {
	}

	// hash值
	public int hashDigest = 0;
	// 魔数
	public String magic = null;
	// cos路径
	public String cosPath = null;
	// 上传文件路径
	public String uploadFile = null;
	// 上传文件状态
	public FileStat uploadFileStat = null;
	// 会话Id
	public String sessionId = null;
	// 开启sha摘要
	public boolean enableShaDigest = true;
	// 上传文件的全文sha
	public String shaDigest = null;
	// 分片大小
	public int sliceSize = 0;
	// 分片信息
	public ArrayList<SlicePart> sliceParts = null;
	// 如果分片第一步init成功，则标为true,避免重复init
	public boolean initFlag = false;

	public boolean serialUpload = true;

	private void assign(SliceCheckPoint scp) {
		this.magic = scp.magic;
		this.hashDigest = scp.hashDigest;
		this.cosPath = scp.cosPath;
		this.uploadFile = scp.uploadFile;
		this.uploadFileStat = scp.uploadFileStat;
		this.enableShaDigest = scp.enableShaDigest;
		this.shaDigest = scp.shaDigest;
		this.sessionId = scp.sessionId;
		this.sliceParts = scp.sliceParts;
		this.initFlag = scp.initFlag;
		this.serialUpload = scp.serialUpload;
		this.sliceSize = scp.sliceSize;
	}

	@Override
	public int hashCode() {
		int hashValue = 1;
		final int prime = 31;
		hashValue = prime * hashValue + ((magic == null) ? 0 : magic.hashCode());
		hashValue = prime * hashValue + ((cosPath == null) ? 0 : cosPath.hashCode());
		hashValue = prime * hashValue + ((uploadFile == null) ? 0 : uploadFile.hashCode());
		hashValue = prime * hashValue + ((uploadFileStat == null) ? 0 : uploadFileStat.hashCode());
		hashValue = prime * hashValue + ((sessionId == null) ? 0 : sessionId.hashCode());
		hashValue = prime * hashValue + ((shaDigest == null) ? 0 : shaDigest.hashCode());
		hashValue = prime * hashValue + ((sliceParts == null) ? 0 : sliceParts.hashCode());
		hashValue = prime * hashValue + sliceSize;
		hashValue = prime * hashValue + new Boolean(initFlag).hashCode();
		hashValue = prime * hashValue + new Boolean(serialUpload).hashCode();
		hashValue = prime * hashValue + new Boolean(enableShaDigest).hashCode();
		return hashValue;
	}

	// Init成功后更新flag
	public synchronized void updateAfterInit(UploadSliceFileContext context) throws AbstractCosException {
		try {
			this.sessionId = context.getSessionId();
			this.enableShaDigest = context.isEnableShaDigest();
			this.shaDigest = context.getEntireFileSha();
			this.initFlag = true;
			this.serialUpload = context.isSerialUpload();
			if (context.isEnableSavePoint()) {
				dump(context.getSavePointFile());
			}
		} catch (IOException e) {
			throw new UnknownException(e.getMessage());
		}
	}

	// 更新分片上传情况
	public synchronized void update(int sliceIndex, boolean uploadCompleted) {
		sliceParts.get(sliceIndex).setUploadCompleted(uploadCompleted);
	}

	// 加载断点文件
	public synchronized void load(String scpFile) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(scpFile);
		ObjectInputStream objIn = new ObjectInputStream(fileIn);
		SliceCheckPoint scp = (SliceCheckPoint) objIn.readObject();
		assign(scp);
		objIn.close();
		fileIn.close();
	}

	// 将上传状态dump到磁盘
	public synchronized void dump(String scpFile) throws IOException {
		this.hashDigest = hashCode();
		FileOutputStream fileOut = new FileOutputStream(scpFile);
		ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
		objOut.writeObject(this);
		objOut.close();
		fileOut.close();
	}

	/**
	 * 根据checkpoint中记录的信息, 是否匹配待上传文件，以及上传文件是否被修改过
	 * 
	 * @param uploadFile
	 * @return
	 * @throws Exception
	 */
	public synchronized boolean isValid(String uploadFile) throws AbstractCosException {
		if (this.magic == null || !this.magic.equals(DEFAULT_MAGIC) || this.hashDigest != hashCode()) {
			return false;
		}

		if (!CommonFileUtils.isLegalFile(uploadFile)) {
			return false;
		}

		if (!this.uploadFile.equals(uploadFile)) {
			return false;
		}

		try {
			if (this.uploadFileStat.getFileSize() != CommonFileUtils.getFileLength(uploadFile)) {
				return false;
			}
			if (this.uploadFileStat.getLastModifiedTime() != CommonFileUtils.getFileLastModified(uploadFile)) {
				return false;
			}
		} catch (Exception e) {
			throw new UnknownException(e.getMessage());
		}

		return true;
	}

}
