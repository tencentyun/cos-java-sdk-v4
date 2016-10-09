package com.qcloud.cos;

import com.qcloud.cos.request.UpdateFileRequest;
import com.qcloud.cos.request.UpdateFolderRequest;
import com.qcloud.cos.request.UploadFileRequest;
import com.qcloud.cos.request.UploadSliceFileRequest;
import com.qcloud.cos.sign.Credentials;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.UnknownException;
import com.qcloud.cos.http.AbstractCosHttpClient;
import com.qcloud.cos.http.DefaultCosHttpClient;
import com.qcloud.cos.op.FileOp;
import com.qcloud.cos.op.FolderOp;
import com.qcloud.cos.request.AbstractBaseRequest;
import com.qcloud.cos.request.CreateFolderRequest;
import com.qcloud.cos.request.DelFileRequest;
import com.qcloud.cos.request.DelFolderRequest;
import com.qcloud.cos.request.GetFileInputStreamRequest;
import com.qcloud.cos.request.GetFileLocalRequest;
import com.qcloud.cos.request.ListFolderRequest;
import com.qcloud.cos.request.StatFileRequest;
import com.qcloud.cos.request.StatFolderRequest;

/**
 * @author chengwu 封装Cos JAVA SDK暴露给用户的接口函数
 */
public class COSClient implements COS {

    private static final Logger LOG = LoggerFactory.getLogger(COSClient.class);

    private ClientConfig config;
    private Credentials cred;
    private AbstractCosHttpClient client;

    private FileOp fileOp;
    private FolderOp folderOp;

    public COSClient(long appId, String secretId, String secretKey) {
        this(new Credentials(appId, secretId, secretKey));
    }

    public COSClient(Credentials cred) {
        this(new ClientConfig(), cred);
    }

    public void setConfig(ClientConfig config) {
        this.config = config;
        this.fileOp.setConfig(config);
        this.folderOp.setConfig(config);
        this.client.shutdown();
        this.client = new DefaultCosHttpClient(config);
        this.fileOp.setHttpClient(this.client);
        this.folderOp.setHttpClient(this.client);
    }

    public void setCred(Credentials cred) {
        this.cred = cred;
        this.fileOp.setCred(cred);
        this.folderOp.setCred(cred);
    }

    public COSClient(ClientConfig config, Credentials cred) {
        this.config = config;
        this.cred = cred;
        this.client = new DefaultCosHttpClient(config);
        fileOp = new FileOp(this.config, this.cred, this.client);
        folderOp = new FolderOp(this.config, this.cred, this.client);
    }

    private void recordException(String methodName, AbstractBaseRequest request, String message) {
        LOG.warn(methodName + " occur a exception, request:{}, message:{}", request, message);
    }

    @Override
    public String updateFolder(UpdateFolderRequest request) {
        try {
            return folderOp.updateFolder(request);
        } catch (AbstractCosException e) {
            recordException("updateFolder", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("updateFolder", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String updateFile(UpdateFileRequest request) {
        try {
            return fileOp.updateFile(request);
        } catch (AbstractCosException e) {
            recordException("updateFile", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("updateFile", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String delFolder(DelFolderRequest request) {
        try {
            return folderOp.delFolder(request);
        } catch (AbstractCosException e) {
            recordException("deleteFolder", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("deleteFolder", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String delFile(DelFileRequest request) {
        try {
            return fileOp.delFile(request);
        } catch (AbstractCosException e) {
            recordException("deleteFile", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("deleteFile", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String statFolder(StatFolderRequest request) {
        try {
            return folderOp.statFolder(request);
        } catch (AbstractCosException e) {
            recordException("getFolderStat", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("getFolderStat", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String statFile(StatFileRequest request) {
        try {
            return fileOp.statFile(request);
        } catch (AbstractCosException e) {
            recordException("getFileStat", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("getFileStat", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String createFolder(CreateFolderRequest request) {
        try {
            return folderOp.createFolder(request);
        } catch (AbstractCosException e) {
            recordException("createFolder", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("createFolder", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String listFolder(ListFolderRequest request) {
        try {
            return folderOp.listFolder(request);
        } catch (AbstractCosException e) {
            recordException("getFolderList", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("getFolderList", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String uploadFile(UploadFileRequest request) {
        try {
            return fileOp.uploadFile(request);
        } catch (AbstractCosException e) {
            recordException("uploadFile", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("uploadFile", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String uploadSingleFile(UploadFileRequest request) {
        try {
            return fileOp.uploadSingleFile(request);
        } catch (AbstractCosException e) {
            recordException("uploadSingleFile", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("uploadSingleFile", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
    public String uploadSliceFile(UploadSliceFileRequest request) {
        try {
            return fileOp.uploadSliceFile(request);
        } catch (AbstractCosException e) {
            recordException("uploadSliceFile", request, e.toString());
            return e.toString();
        } catch (Exception e) {
            UnknownException e1 = new UnknownException(e.toString());
            recordException("uploadSliceFile", request, e1.toString());
            return e1.toString();
        }
    }

    @Override
	public InputStream getFileInputStream(GetFileInputStreamRequest request) throws Exception {
		try {
			return fileOp.getFileInputStream(request);
		} catch (AbstractCosException e) {
			recordException("getFileInputStream", request, e.toString());
			throw new Exception(e.getMessage());
		} catch (Exception e) {
			UnknownException e1 = new UnknownException(e.toString());
			recordException("getFileInputStream", request, e1.toString());
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String getFileLocal(GetFileLocalRequest request) {
		try {
			return fileOp.getFileLocal(request);
		} catch (AbstractCosException e) {
			recordException("getFileLocalRequest", request, e.toString());
			return e.toString();
		} catch (Exception e) {
			UnknownException e1 = new UnknownException(e.toString());
			recordException("getFileLocalRequest", request, e1.toString());
			return e1.toString();
		}
	}
	@Override
    public void shutdown() {
        this.client.shutdown();
    }

}
