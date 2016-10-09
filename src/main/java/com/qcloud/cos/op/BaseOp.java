package com.qcloud.cos.op;

import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.common_utils.CommonPathUtils;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.http.AbstractCosHttpClient;
import com.qcloud.cos.http.HttpContentType;
import com.qcloud.cos.http.HttpMethod;
import com.qcloud.cos.http.HttpRequest;
import com.qcloud.cos.http.RequestBodyKey;
import com.qcloud.cos.http.RequestBodyValue;
import com.qcloud.cos.http.RequestHeaderKey;
import com.qcloud.cos.http.RequestHeaderValue;
import com.qcloud.cos.request.AbstractBaseRequest;
import com.qcloud.cos.request.AbstractDelRequest;
import com.qcloud.cos.request.AbstractStatRequest;
import com.qcloud.cos.sign.Credentials;
import com.qcloud.cos.sign.Sign;

/**
 * @author chengwu 封装目录和文件共同的操作
 */
public abstract class BaseOp {
    // 配置信息
    protected ClientConfig config;
    // 鉴权信息
    protected Credentials cred;
    // http请求发送对象
    protected AbstractCosHttpClient httpClient;

    public BaseOp(ClientConfig config, Credentials cred, AbstractCosHttpClient httpClient) {
        super();
        this.config = config;
        this.cred = cred;
        this.httpClient = httpClient;
    }

    public void setConfig(ClientConfig config) {
        this.config = config;
    }

    public void setCred(Credentials cred) {
        this.cred = cred;
    }

    public void setHttpClient(AbstractCosHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 根据APPID, BUCKET, COS_PATH生成经过URL编码的URL
     * 
     * @param request 基本类型的请求
     * @return URL字符串
     * @throws AbstractCosException
     */
    protected String buildUrl(AbstractBaseRequest request) throws AbstractCosException {
        String endPoint = new StringBuilder().append(this.config.getUploadCosEndPointPrefix())
                .append(this.config.getUploadCosEndPointDomain())
                .append(this.config.getUploadCosEndPointSuffix()).toString();
        long appId = this.cred.getAppId();
        String bucketName = request.getBucketName();
        String cosPath = request.getCosPath();
        cosPath = CommonPathUtils.encodeRemotePath(cosPath);
        return String.format("%s/%d/%s%s", endPoint, appId, bucketName, cosPath);
    }

    /**
     * 删除文件或者目录
     * 
     * @param request 删除文件或者目录的请求, 类型为DelFileRequest或者DelFolderRequest
     * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功, 其他为失败,
     *         message为success或者失败原因
     * @throws AbstractCosException SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
     */
    protected String delBase(final AbstractDelRequest request) throws AbstractCosException {
        request.check_param();

        String url = buildUrl(request);
        String sign =
                Sign.getOneEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
        httpRequest.addHeader(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);
        httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
        httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.DELETE);
        httpRequest.setMethod(HttpMethod.POST);
        httpRequest.setContentType(HttpContentType.APPLICATION_JSON);
        return httpClient.sendHttpRequest(httpRequest);
    }

    /**
     * 获取文件或者目录的属性
     * 
     * @param request 文件或者目录的属性请求, 类型为StatFileRequest或者StatFolderRequest
     * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功, 其他为失败,
     *         message为success或者失败原因
     * @throws AbstractCosException SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
     */
    protected String statBase(final AbstractStatRequest request) throws AbstractCosException {
        request.check_param();

        String url = buildUrl(request);
        long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
        String sign = Sign.getPeriodEffectiveSign(request.getBucketName(), request.getCosPath(),
                this.cred, signExpired);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
        httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
        httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.STAT);
        httpRequest.setMethod(HttpMethod.GET);

        return httpClient.sendHttpRequest(httpRequest);
    }
}
