package com.qcloud.cos.meta;

import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.cos.common_utils.CommonFileUtils;
import com.qcloud.cos.exception.UnknownException;
import com.qcloud.cos.http.AbstractCosHttpClient;
import com.qcloud.cos.http.HttpContentType;
import com.qcloud.cos.http.HttpMethod;
import com.qcloud.cos.http.HttpRequest;
import com.qcloud.cos.http.RequestBodyKey;
import com.qcloud.cos.http.RequestBodyValue;
import com.qcloud.cos.http.RequestHeaderKey;
import com.qcloud.cos.sign.Credentials;
import com.qcloud.cos.sign.Sign;

/**
 * @author chengwu 执行分片上传的任务, 每一个任务由一个线程执行
 */
public class SliceFileDataTask implements Callable<JSONObject> {

    private static final Logger LOG = LoggerFactory.getLogger(SliceFileDataTask.class);

    private int TaskId;
    private int sliceIndex;
    private UploadSliceFileContext context;
    private AbstractCosHttpClient httpClient;
    private Credentials cred;
    private String url;
    private long signExpired;

    public SliceFileDataTask(int taskId, int sliceIndex, UploadSliceFileContext context,
            AbstractCosHttpClient httpClient, Credentials cred, String url, long signExpired) {
        super();
        TaskId = taskId;
        this.sliceIndex = sliceIndex;
        this.context = context;
        this.httpClient = httpClient;
        this.cred = cred;
        this.url = url;
        this.signExpired = signExpired;
    }

    @Override
    public JSONObject call() throws Exception {
        try {
            HttpRequest httpRequest = new HttpRequest();
            SlicePart slicePart = context.sliceParts.get(sliceIndex);
            httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPLOAD_SLICE_DATA);
            if (this.context.isEnableShaDigest()) {
                httpRequest.addParam(RequestBodyKey.SHA, context.getEntireFileSha());
            }

            httpRequest.addParam(RequestBodyKey.SESSION, context.getSessionId());
            httpRequest.addParam(RequestBodyKey.OFFSET, String.valueOf(slicePart.getOffset()));
            String sliceContent = "";
            if (this.context.isUploadFromBuffer()) {
                sliceContent = new String(context.getContentBuffer(),
                        new Long(slicePart.getOffset()).intValue(), slicePart.getSliceSize(),
                        Charset.forName("ISO-8859-1"));
            } else {
                sliceContent = CommonFileUtils.getFileContent(context.getLocalPath(),
                        slicePart.getOffset(), slicePart.getSliceSize());
            }
            httpRequest.addParam(RequestBodyKey.FILE_CONTENT, sliceContent);

            long signExpired = System.currentTimeMillis() / 1000 + this.signExpired;
            String sign = Sign.getPeriodEffectiveSign(context.getBucketName(), context.getCosPath(),
                    this.cred, signExpired);
            httpRequest.addHeader(RequestHeaderKey.Authorization, sign);

            httpRequest.setUrl(this.url);
            httpRequest.setMethod(HttpMethod.POST);
            httpRequest.setContentType(HttpContentType.MULTIPART_FORM_DATA);

            String resultStr = httpClient.sendHttpRequest(httpRequest);
            LOG.debug("sliceFileDataTask: " + this.toString() + ", result: " + resultStr);
            JSONObject resultJson = new JSONObject(resultStr);
            return resultJson;
        } catch (Exception e) {
            String errMsg = new StringBuilder().append("taskInfo:").append(this.toString())
                    .append(", Exception:").append(e.toString()).toString();
            LOG.error(errMsg);
            throw new UnknownException(errMsg);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TaskId:").append(TaskId).append(", SliceIndex:").append(sliceIndex)
                .append(", localPath:").append(context.getLocalPath()).append(", uploadUrl:")
                .append(this.url);
        return sb.toString();
    }
}
