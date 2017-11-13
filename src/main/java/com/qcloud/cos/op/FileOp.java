package com.qcloud.cos.op;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.common_utils.CommonCodecUtils;
import com.qcloud.cos.common_utils.CommonFileUtils;
import com.qcloud.cos.common_utils.CommonPathUtils;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.ParamException;
import com.qcloud.cos.exception.UnknownException;
import com.qcloud.cos.http.AbstractCosHttpClient;
import com.qcloud.cos.http.HttpContentType;
import com.qcloud.cos.http.HttpMethod;
import com.qcloud.cos.http.HttpRequest;
import com.qcloud.cos.http.RequestBodyKey;
import com.qcloud.cos.http.RequestBodyValue;
import com.qcloud.cos.http.RequestHeaderKey;
import com.qcloud.cos.http.RequestHeaderValue;
import com.qcloud.cos.http.ResponseBodyKey;
import com.qcloud.cos.meta.InsertOnly;
import com.qcloud.cos.meta.SliceFileDataTask;
import com.qcloud.cos.meta.UploadSliceFileContext;
import com.qcloud.cos.request.DelFileRequest;
import com.qcloud.cos.request.GetFileInputStreamRequest;
import com.qcloud.cos.request.GetFileLocalRequest;
import com.qcloud.cos.request.ListPartsRequest;
import com.qcloud.cos.request.MoveFileRequest;
import com.qcloud.cos.request.StatFileRequest;
import com.qcloud.cos.request.UpdateFileRequest;
import com.qcloud.cos.request.UploadFileRequest;
import com.qcloud.cos.request.UploadSliceFileRequest;
import com.qcloud.cos.sign.Credentials;
import com.qcloud.cos.sign.Sign;

/**
 * @author chengwu 此类封装了文件操作
 */
public class FileOp extends BaseOp {

    private static final Logger LOG = LoggerFactory.getLogger(FileOp.class);

    public FileOp(ClientConfig config, Credentials cred, AbstractCosHttpClient client) {
        super(config, cred, client);
    }

    private String buildGetFileUrl(GetFileInputStreamRequest request) throws AbstractCosException {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(this.config.getDownCosEndPointPrefix()).append(request.getBucketName())
                .append("-").append(this.cred.getAppId()).append(".");
        if (request.isUseCDN()) {
            strBuilder.append("file.myqcloud.com");
        } else {
            strBuilder.append(this.config.getDownCosEndPointDomain());
        }
        strBuilder.append(CommonPathUtils.encodeRemotePath(request.getCosPath()));
        String url = strBuilder.toString();
        return url;
    }

    /**
     * 更新文件属性请求
     *
     * @param request 更新文件属性请求
     * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功, 其他为失败,
     *         message为success或者失败原因
     * @throws AbstractCosException SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
     */
    public String updateFile(final UpdateFileRequest request) throws AbstractCosException {
        request.check_param();

        String url = buildUrl(request);
        String sign =
                Sign.getOneEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
        httpRequest.addHeader(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);
        httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
        httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPDATE);
        int updateFlag = request.getUpdateFlag();
        if ((updateFlag & 0x01) != 0) {
            httpRequest.addParam(RequestBodyKey.BIZ_ATTR, request.getBizAttr());
        }
        if ((updateFlag & 0x40) != 0) {
            String customHeaderStr = new JSONObject(request.getCustomHeaders()).toString();
            httpRequest.addParam(RequestBodyKey.CUSTOM_HEADERS, customHeaderStr);
        }
        if ((updateFlag & 0x80) != 0) {
            httpRequest.addParam(RequestBodyKey.AUTHORITY, request.getAuthority().toString());
        }
        httpRequest.setMethod(HttpMethod.POST);
        httpRequest.setContentType(HttpContentType.APPLICATION_JSON);
        return httpClient.sendHttpRequest(httpRequest);
    }

    /**
     * 删除文件请求
     *
     * @param request 删除文件请求
     * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功, 其他为失败,
     *         message为success或者失败原因
     * @throws AbstractCosException SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
     */
    public String delFile(DelFileRequest request) throws AbstractCosException {
        return super.delBase(request);
    }

    /**
     * 获取文件属性请求
     *
     * @param request 获取文件属性请求
     * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功, 其他为失败,
     *         message为success或者失败原因
     * @throws AbstractCosException SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
     */
    public String statFile(StatFileRequest request) throws AbstractCosException {
        return super.statBase(request);
    }

    /**
     * 上传文件请求, 对小文件(8MB以下使用单文件上传接口）, 大文件使用分片上传接口
     *
     * @param request 上传文件请求
     * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功, 其他为失败,
     *         message为success或者失败原因
     * @throws AbstractCosException SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
     */
    public String uploadFile(UploadFileRequest request) throws AbstractCosException {
        request.check_param();

        String localPath = request.getLocalPath();
        long fileSize = 0;
        if (request.isUploadFromBuffer()) {
            fileSize = request.getContentBufer().length;
        } else {
            try {
                fileSize = CommonFileUtils.getFileLength(localPath);
            } catch (Exception e) {
                throw new UnknownException(e.toString());
            }
        }

        long suitSingleFileSize = 8 * 1024 * 1024;
        if (fileSize < suitSingleFileSize) {
            return uploadSingleFile(request);
        } else {
            UploadSliceFileRequest sliceRequest = new UploadSliceFileRequest(request);
            sliceRequest.setInsertOnly(request.getInsertOnly());
            if (request.isUploadFromBuffer()) {
                sliceRequest.setContentBufer(request.getContentBufer());
            }
            sliceRequest.setEnableShaDigest(request.isEnableShaDigest());
            sliceRequest.setTaskNum(request.getTaskNum());
            return uploadSliceFile(sliceRequest);
        }
    }

    /**
     * 上传单文件请求, 不分片
     *
     * @param request 上传文件请求
     * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功, 其他为失败,
     *         message为success或者失败原因
     * @throws AbstractCosException SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
     */
    public String uploadSingleFile(UploadFileRequest request) throws AbstractCosException {
        request.check_param();

        String localPath = request.getLocalPath();
        long fileSize = 0;
        if (request.isUploadFromBuffer()) {
            fileSize = request.getContentBufer().length;
        } else {
            try {
                fileSize = CommonFileUtils.getFileLength(localPath);
            } catch (Exception e) {
                throw new UnknownException(e.toString());
            }
        }
        // 单文件上传上限不超过20MB
        if (fileSize > 20 * 1024 * 1024) {
            throw new ParamException("file is to big, please use uploadFile interface!");
        }

        String fileContent = "";
        String shaDigest = "";
        try {
            if (request.isUploadFromBuffer()) {
                fileContent = new String(request.getContentBufer(), Charset.forName("ISO-8859-1"));
                shaDigest = CommonCodecUtils.getBufferSha1(request.getContentBufer());
            } else {
                fileContent = CommonFileUtils.getFileContent(localPath);
                shaDigest = CommonCodecUtils.getEntireFileSha1(localPath);
            }
        } catch (Exception e) {
            throw new UnknownException(e.toString());
        }

        String url = buildUrl(request);
        long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
        String sign = Sign.getPeriodEffectiveSign(request.getBucketName(), request.getCosPath(),
                this.cred, signExpired);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
        httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());

        httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPLOAD);
        httpRequest.addParam(RequestBodyKey.SHA, shaDigest);
        httpRequest.addParam(RequestBodyKey.BIZ_ATTR, request.getBizAttr());
        httpRequest.addParam(RequestBodyKey.FILE_CONTENT, fileContent);
        httpRequest.addParam(RequestBodyKey.INSERT_ONLY,
                String.valueOf(request.getInsertOnly().ordinal()));

        httpRequest.setMethod(HttpMethod.POST);
        httpRequest.setContentType(HttpContentType.MULTIPART_FORM_DATA);

        String retStr = httpClient.sendHttpRequest(httpRequest);
        if (request.getInsertOnly() != InsertOnly.OVER_WRITE) {
            return retStr;
        }
        // 对于Overwrite类型，覆盖上传失败，做特殊处理，删掉重新传
        JSONObject retJson = new JSONObject(retStr);
        if (retJson.getInt("code") == 0) {
            return retStr;
        }
        // 1. Delete
        DelFileRequest del_request =
                new DelFileRequest(request.getBucketName(), request.getCosPath());
        String delRet = delFile(del_request);
        JSONObject delJson = new JSONObject(delRet);
        if (delJson.getInt("code") != 0) {
            return retStr;
        }
        // 2. Upload Again
        return httpClient.sendHttpRequest(httpRequest);
    }

    /**
     * 分片上传文件
     *
     * @param request 分片上传请求
     * @return 服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public String uploadSliceFile(UploadSliceFileRequest request) throws AbstractCosException {
        request.check_param();
        UploadSliceFileContext context = new UploadSliceFileContext(request);
        context.setUrl(buildUrl(request));
        String retStr = uploadFileWithCheckPoint(context);

        if (request.getInsertOnly() != InsertOnly.OVER_WRITE) {
            return retStr;
        }
        // 对于Overwrite类型，覆盖上传失败，做特殊处理，删掉重新传
        JSONObject retJson = new JSONObject(retStr);
        if (retJson.getInt("code") == 0) {
            return retStr;
        }
        // 1. Delete
        DelFileRequest del_request =
                new DelFileRequest(request.getBucketName(), request.getCosPath());
        String delRet = delFile(del_request);
        JSONObject delJson = new JSONObject(delRet);
        if (delJson.getInt("code") != 0) {
            return retStr;
        }
        // 2. Upload Again
        retStr = uploadFileWithCheckPoint(context);
        retJson = new JSONObject(retStr);
        if (retJson.getInt("code") != 0) {
            del_request = new DelFileRequest(request.getBucketName(), request.getCosPath());
            delFile(del_request);
        }
        return retStr;
    }

    /**
     * 移动文件请求(重命名)
     *
     * @param request 移动文件请求
     * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功, 其他为失败,
     *         message为success或者失败原因
     * @throws AbstractCosException SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
     */
    public String moveFile(MoveFileRequest request) throws AbstractCosException {
        request.check_param();

        String url = buildUrl(request);
        String sign =
                Sign.getOneEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
        httpRequest.addHeader(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);
        httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
        httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.MOVE);
        httpRequest.addParam(RequestBodyKey.DEST_FIELD, request.getDstCosPath());
        httpRequest.addParam(RequestBodyKey.TO_OVER_WRITE,
                String.valueOf(request.getOverWrite().ordinal()));
        httpRequest.setMethod(HttpMethod.POST);
        httpRequest.setContentType(HttpContentType.APPLICATION_JSON);
        return httpClient.sendHttpRequest(httpRequest);
    }

    // 断点续传
    private String uploadFileWithCheckPoint(UploadSliceFileContext context)
            throws AbstractCosException {
        JSONObject initResult = sendSliceInit(context);

        int initResultCode = initResult.getInt(ResponseBodyKey.CODE);
        // 4019文件未上传完成,这时候应该用断点续传
        if (initResultCode != 0 && initResultCode != -4019) {
            return initResult.toString();
        }

        if (initResultCode == 0) {
            JSONObject data = initResult.getJSONObject(ResponseBodyKey.DATA);
            if (data.has(ResponseBodyKey.Data.ACCESS_URL)) {
                return initResult.toString();
            }
            if (data.has(ResponseBodyKey.Data.SERIAL_UPLOAD)
                    && data.getInt(ResponseBodyKey.Data.SERIAL_UPLOAD) == 1) {
                LOG.debug("SERIAL_UPLOAD is true");
                context.setSerialUpload(true);
            } else {
                LOG.debug("SERIAL_UPLOAD is false");;
                context.setSerialUpload(false);
            }
            context.setSessionId(data.getString(ResponseBodyKey.Data.SESSION));
            // 如果服务端返回的slice_slize和用户要求的不一致, 则重新分片
            if (data.getInt(ResponseBodyKey.Data.SLICE_SIZE) != context.getSliceSize()) {
                context.setSliceSize(data.getInt(ResponseBodyKey.Data.SLICE_SIZE));
            }
        } else {
            ListPartsRequest listPartsRequest =
                    new ListPartsRequest(context.getBucketName(), context.getCosPath());
            String listPartsResult = sliceListParts(listPartsRequest);
            JSONObject listPartsJson = new JSONObject(listPartsResult);
            if (listPartsJson.getInt(ResponseBodyKey.CODE) != 0) {
                return listPartsJson.toString();
            }

            JSONObject data = listPartsJson.getJSONObject(ResponseBodyKey.DATA);
            if (data.has(ResponseBodyKey.Data.LISTPARTS)) {
                JSONArray listPartsJsonArry = data.getJSONArray(ResponseBodyKey.Data.LISTPARTS);
                context.setUploadCompleteParts(listPartsJsonArry);
            }
            context.setSessionId(data.getString(ResponseBodyKey.Data.SESSION));
        }
        
        context.prepareUploadPartsInfo();
        // 并行发送数据分片
        JSONObject sendParallelRet = sendSliceDataParallel(context);
        if (sendParallelRet.getInt(ResponseBodyKey.CODE) != 0) {
            return sendParallelRet.toString();
        }

        // 发送finish分片
        JSONObject finishRet = sendSliceFinish(context);
        return finishRet.toString();
    }

    /**
     * 分片上传第一步，发送init分片
     *
     * @param context 分片上传请求上下文
     * @return 服务器端返回的操作结果，code为0表示成功，其他包括sessionId、offset等，具体参见文档手册
     * @throws Exception
     */
    private JSONObject sendSliceInit(UploadSliceFileContext context) throws AbstractCosException {
        String localPath = context.getLocalPath();
        long fileSize = context.getFileSize();
        int sliceSize = context.getSliceSize();

        StringBuilder entireDigestSb = new StringBuilder();
        String slicePartDigest = "";
        try {
            if (context.isEnableShaDigest()) {
                if (context.isUploadFromBuffer()) {
                    slicePartDigest = CommonCodecUtils.getSlicePartSha1(context.getContentBuffer(),
                            sliceSize, entireDigestSb);
                } else {
                    slicePartDigest =
                            CommonCodecUtils.getSlicePartSha1(localPath, sliceSize, entireDigestSb);
                }
                context.setEntireFileSha(entireDigestSb.toString());
                LOG.debug("slicePartDigest: " + slicePartDigest);
            }
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }

        String url = context.getUrl();
        long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
        String sign = Sign.getPeriodEffectiveSign(context.getBucketName(), context.getCosPath(),
                this.cred, signExpired);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
        httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
        httpRequest.addParam(RequestBodyKey.FILE_SIZE, String.valueOf(fileSize));
        httpRequest.addParam(RequestBodyKey.SLICE_SIZE, String.valueOf(sliceSize));
        httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPLOAD_SLICE_INIT);
        httpRequest.addParam(RequestBodyKey.INSERT_ONLY,
                String.valueOf(context.getInsertOnly().ordinal()));
        httpRequest.addParam(RequestBodyKey.BIZ_ATTR, context.getBizAttr());
        if (context.isEnableShaDigest()) {
            httpRequest.addParam(RequestBodyKey.SHA, entireDigestSb.toString());
            httpRequest.addParam(RequestBodyKey.UPLOAD_PARTS, slicePartDigest);
        }

        httpRequest.setMethod(HttpMethod.POST);
        httpRequest.setContentType(HttpContentType.MULTIPART_FORM_DATA);

        JSONObject resultJson = null;
        String resultStr = this.httpClient.sendHttpRequest(httpRequest);
        LOG.debug("sendSliceInit, resultStr: " + resultStr);
        resultJson = new JSONObject(resultStr);
        return resultJson;
    }

    /**
     * 分片上传第二步，发送数据分片
     *
     * @param context 分片上传请求
     * @return 服务器端返回的操作结果，code为0表示成功
     * @throws Exception
     */
    private JSONObject sendSliceDataParallel(UploadSliceFileContext context)
            throws AbstractCosException {
        List<Future<JSONObject>> allSliceTasks = new ArrayList<>();
        // 默认串行执行,只用一个线程，如果server端支持并行上传，则用多个线程执行
        int threadNum = 1;
        if (!context.isSerialUpload()) {
            threadNum = context.getTaskNum();
        }
        ExecutorService service = Executors.newFixedThreadPool(threadNum);

        String url = context.getUrl();
        long signExpired = this.config.getSignExpired();
        for (int sliceIndex = 0; sliceIndex < context.sliceParts.size(); ++sliceIndex) {
            if (!context.sliceParts.get(sliceIndex).isUploadCompleted()) {
                SliceFileDataTask dataTask = new SliceFileDataTask(sliceIndex, sliceIndex, context,
                        httpClient, cred, url, signExpired);
                allSliceTasks.add(service.submit(dataTask));
            }
        }
        service.shutdown();

        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            service.shutdownNow();
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }

        JSONObject taskResult = null;
        if (allSliceTasks.size() == 0) {
            taskResult = new JSONObject();
            taskResult.put(ResponseBodyKey.CODE, 0);
            return taskResult;
        }

        for (Future<JSONObject> task : allSliceTasks) {
            try {
                taskResult = task.get();
            } catch (Exception e) {
                throw new UnknownException(e.getMessage());
            }
            if (taskResult.getInt(ResponseBodyKey.CODE) != 0) {
                return taskResult;
            }
        }

        return taskResult;
    }

    /**
     * 最后一步, 发送finish分片
     *
     * @return 服务器端返回的操作结果，成功code为0
     * @throws Exception
     */
    private JSONObject sendSliceFinish(UploadSliceFileContext context) throws AbstractCosException {
        String url = context.getUrl();

        long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
        String sign = Sign.getPeriodEffectiveSign(context.getBucketName(), context.getCosPath(),
                this.cred, signExpired);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
        httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
        httpRequest.addParam(RequestBodyKey.SESSION, context.getSessionId());
        httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPLOAD_SLICE_FINISH);
        if (context.isEnableShaDigest()) {
            httpRequest.addParam(RequestBodyKey.SHA, context.getEntireFileSha());
        }
        httpRequest.addParam(RequestBodyKey.FILE_SIZE, String.valueOf(context.getFileSize()));

        httpRequest.setContentType(HttpContentType.MULTIPART_FORM_DATA);
        httpRequest.setMethod(HttpMethod.POST);

        JSONObject resultJson = null;
        String resultStr = this.httpClient.sendHttpRequest(httpRequest);
        resultJson = new JSONObject(resultStr);
        LOG.debug("sendSliceFinish, resultStr: " + resultStr);
        return resultJson;
    }

    public String sliceListParts(ListPartsRequest request) throws AbstractCosException {
        request.check_param();

        String url = buildUrl(request);
        long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
        String sign = Sign.getPeriodEffectiveSign(request.getBucketName(), request.getCosPath(),
                this.cred, signExpired);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
        httpRequest.addHeader(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);
        httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
        httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPLOAD_SLICE_LIST);
        httpRequest.setMethod(HttpMethod.POST);
        httpRequest.setContentType(HttpContentType.APPLICATION_JSON);
        return httpClient.sendHttpRequest(httpRequest);
    }

    public String getFileLocal(GetFileLocalRequest request) throws AbstractCosException {
        InputStream in = getFileInputStream(request);
        BufferedInputStream bis = new BufferedInputStream(in);
        OutputStream out = null;
        try {
            out = new FileOutputStream(new File(request.getLocalPath()));
        } catch (FileNotFoundException e) {
            throw new UnknownException(e.getMessage());
        }
        BufferedOutputStream bos = new BufferedOutputStream(out);
        int inByte;
        try {
            while ((inByte = bis.read()) != -1)
                bos.write(inByte);
        } catch (IOException e) {
            throw new UnknownException(e.getMessage());
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (IOException e) {
                throw new UnknownException(e.getMessage());
            }
        }
        JSONObject retJson = new JSONObject();
        retJson.put(ResponseBodyKey.CODE, 0);
        retJson.put(ResponseBodyKey.MESSAGE, "SUCCESS");
        return retJson.toString();
    }

    public InputStream getFileInputStream(GetFileInputStreamRequest request)
            throws AbstractCosException {
        String url = buildGetFileUrl(request);
        long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
        String sign = Sign.getDownLoadSign(request.getBucketName(), request.getCosPath(), this.cred,
                signExpired);

        StringBuilder rangeBuilder = new StringBuilder();
        if (request.getRangeStart() != 0 || request.getRangeEnd() != Long.MAX_VALUE) {
            rangeBuilder.append("bytes=").append(request.getRangeStart()).append("-");
            rangeBuilder.append(request.getRangeEnd());
        }

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
        if (!rangeBuilder.toString().isEmpty()) {
            httpRequest.addHeader(RequestHeaderKey.RANGE, rangeBuilder.toString());
        }
        if (!request.getReferer().isEmpty()) {
            httpRequest.addHeader(RequestHeaderKey.REFERER, request.getReferer());
        }
        httpRequest.addParam(RequestHeaderKey.SIGN, sign);
        return httpClient.getFileInputStream(httpRequest);
    }
}
