package com.qcloud.cos.op;

import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.http.AbstractCosHttpClient;
import com.qcloud.cos.http.HttpContentType;
import com.qcloud.cos.http.HttpMethod;
import com.qcloud.cos.http.HttpRequest;
import com.qcloud.cos.http.RequestBodyKey;
import com.qcloud.cos.http.RequestBodyValue;
import com.qcloud.cos.http.RequestHeaderKey;
import com.qcloud.cos.http.RequestHeaderValue;
import com.qcloud.cos.request.CreateFolderRequest;
import com.qcloud.cos.request.DelFolderRequest;
import com.qcloud.cos.request.ListFolderRequest;
import com.qcloud.cos.request.StatFolderRequest;
import com.qcloud.cos.request.UpdateFolderRequest;
import com.qcloud.cos.sign.Credentials;
import com.qcloud.cos.sign.Sign;

/**
 * @author chengwu
 * 此类封装了常用的目录操作
 */
public class FolderOp extends BaseOp {

    public FolderOp(ClientConfig config, Credentials cred, AbstractCosHttpClient client) {
        super(config, cred, client);
    }

	/**
	 * 更新目录属性请求
	 * 
	 * @param request
	 *            更新目录属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
    public String updateFolder(UpdateFolderRequest request) throws AbstractCosException {
		request.check_param();

		String url = buildUrl(request);
		String sign = Sign.getOneEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setUrl(url);
		httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
		httpRequest.addHeader(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);
		httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
		httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPDATE);
		httpRequest.addParam(RequestBodyKey.BIZ_ATTR, request.getBizAttr());

		httpRequest.setMethod(HttpMethod.POST);
		httpRequest.setContentType(HttpContentType.APPLICATION_JSON);
		return httpClient.sendHttpRequest(httpRequest);
    }

	/**
	 * 删除目录请求
	 * 
	 * @param request
	 *            删除目录请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
    public String delFolder(DelFolderRequest request) throws AbstractCosException {
    	return super.delBase(request);
    }

	/**
	 * 获取目录属性请求
	 * 
	 * @param request
	 *            获取目录属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
    public String statFolder(StatFolderRequest request) throws AbstractCosException {
    	return super.statBase(request);
    }

	/**
	 * 创建目录请求
	 * 
	 * @param request
	 *            创建目录属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
    public String createFolder(CreateFolderRequest request) throws AbstractCosException {
		request.check_param();

		String url = buildUrl(request);
		long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
		String sign = Sign.getPeriodEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred, signExpired);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setUrl(url);
		httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
		httpRequest.addHeader(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);
		httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
		httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.CREATE);
		httpRequest.addParam(RequestBodyKey.BIZ_ATTR, request.getBizAttr());

		httpRequest.setMethod(HttpMethod.POST);
		httpRequest.setContentType(HttpContentType.APPLICATION_JSON);
		return httpClient.sendHttpRequest(httpRequest);
    }

	/**
	 * 获取目录列表请求
	 * 
	 * @param request
	 *            list目录属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
    public String listFolder(ListFolderRequest request) throws AbstractCosException {

		request.check_param();
		request.setCosPath(request.getCosPath() + request.getPrefix());

		String url = buildUrl(request);
		long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
		String sign = Sign.getPeriodEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred, signExpired);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setUrl(url);
		httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
		httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
		httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.LIST);
		httpRequest.addParam(RequestBodyKey.NUM, String.valueOf(request.getNum()));
		httpRequest.addParam(RequestBodyKey.LIST_FLAG, String.valueOf(request.getListFlag()));
		httpRequest.addParam(RequestBodyKey.CONTEXT, request.getContext());
		httpRequest.setMethod(HttpMethod.GET);

		return httpClient.sendHttpRequest(httpRequest);
    }

}
