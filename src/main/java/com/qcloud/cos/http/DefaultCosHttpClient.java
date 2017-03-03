package com.qcloud.cos.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.ParamException;
import com.qcloud.cos.exception.ServerException;
import com.qcloud.cos.exception.UnknownException;
import com.qcloud.cos.meta.COSObjectInputStream;

/**
 * @author chengwu 封装Http发送请求类
 */
public class DefaultCosHttpClient extends AbstractCosHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCosHttpClient.class);

    public DefaultCosHttpClient(ClientConfig config) {
        super(config);
    }

    // 获得异常发生时的返回信息
    private String getExceptionMsg(HttpRequest httpRequest, String exceptionStr) {
        String errMsg = new StringBuilder("HttpRequest:").append(httpRequest.toString())
                .append("\nException:").append(exceptionStr).toString();
        LOG.error(errMsg);
        return errMsg;
    }

    /**
     * Get请求函数
     * 
     * @param url
     * @param headers 额外添加的Http头部
     * @param params GET请求的参数
     * @return Cos服务器返回的字符串
     * @throws Exception
     */
    @Override
    protected String sendGetRequest(HttpRequest httpRequest) throws AbstractCosException {
        String url = httpRequest.getUrl();
        HttpGet httpGet = null;
        String responseStr = "";
        int retry = 0;
        int maxRetryCount = this.config.getMaxFailedRetry();
        while (retry < maxRetryCount) {
            try {
                URIBuilder urlBuilder = new URIBuilder(url);
                for (String paramKey : httpRequest.getParams().keySet()) {
                    urlBuilder.addParameter(paramKey, httpRequest.getParams().get(paramKey));
                }
                httpGet = new HttpGet(urlBuilder.build());
            } catch (URISyntaxException e) {
                String errMsg = "Invalid url:" + url;
                LOG.error(errMsg);
                throw new ParamException(errMsg);
            }

            httpGet.setConfig(requestConfig);
            setHeaders(httpGet, httpRequest.getHeaders());

            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpGet);
                int http_statuscode = httpResponse.getStatusLine().getStatusCode();

                if (http_statuscode >= 500 && http_statuscode <= 599) {
                    String errMsg = String.format("http status code is %d, response body: %s", http_statuscode, getResponseString(httpResponse));
                    throw new IOException(errMsg);
                }

                responseStr = getResponseString(httpResponse);
                new JSONObject(responseStr);
                return responseStr;
            } catch (ParseException | IOException e) {
                httpGet.abort();
                ++retry;
                if (retry == maxRetryCount) {
                    String errMsg = getExceptionMsg(httpRequest, e.toString());
                    throw new ServerException(errMsg);
                }
            } catch (JSONException e) {
                String errMsg = String.format(
                        "server response is not json, httpRequest: %s, httpResponse: %s, responseStr: %s",
                        httpRequest.toString(), httpResponse.toString(), responseStr);
                throw new ServerException(errMsg);
            } finally {
                httpGet.releaseConnection();
            }
        }
        return responseStr;

    }

    @Override
    protected String sendPostRequest(HttpRequest httpRequest) throws AbstractCosException {
        String url = httpRequest.getUrl();
        String responseStr = "";
        int retry = 0;
        int maxRetryCount = this.config.getMaxFailedRetry();
        while (retry < maxRetryCount) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);

            Map<String, String> params = httpRequest.getParams();
            setHeaders(httpPost, httpRequest.getHeaders());

            if (httpRequest.getContentType() == HttpContentType.APPLICATION_JSON) {
                setJsonEntity(httpPost, params);
            } else if (httpRequest.getContentType() == HttpContentType.MULTIPART_FORM_DATA) {
                try {
                    setMultiPartEntity(httpPost, params);
                } catch (Exception e) {
                    throw new UnknownException(e.toString());
                }
            }

            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpPost);
                int http_statuscode = httpResponse.getStatusLine().getStatusCode();
                if (http_statuscode >= 500 && http_statuscode <= 599) {
                    String errMsg = String.format("http status code is %d, response body: %s", http_statuscode, getResponseString(httpResponse));
                    throw new IOException(errMsg);
                }
                responseStr = getResponseString(httpResponse);
                new JSONObject(responseStr);
                return responseStr;
            } catch (ParseException | IOException e) {
                httpPost.abort();
                ++retry;
                if (retry == maxRetryCount) {
                    String errMsg = getExceptionMsg(httpRequest, e.toString());
                    throw new ServerException(errMsg);
                }
            } catch (JSONException e) {
                String errMsg = String.format(
                        "server response is not json, httpRequest: %s, httpResponse: %s, responseStr: %s",
                        httpRequest.toString(), httpResponse.toString(), responseStr);
                throw new ServerException(errMsg);
            } finally {
                httpPost.releaseConnection();
            }
        }
        return responseStr;
    }

    @Override
    public InputStream getFileInputStream(HttpRequest httpRequest) throws AbstractCosException {
        String url = httpRequest.getUrl();
        int retry = 0;
        int maxRetryCount = this.config.getMaxFailedRetry();
        while (retry < maxRetryCount) {
            HttpGet httpGet = null;
            try {
                URIBuilder urlBuilder = new URIBuilder(url);
                for (String paramKey : httpRequest.getParams().keySet()) {
                    urlBuilder.addParameter(paramKey, httpRequest.getParams().get(paramKey));
                }
                httpGet = new HttpGet(urlBuilder.build());
            } catch (URISyntaxException e) {
                String errMsg = "Invalid url:" + url;
                LOG.error(errMsg);
                throw new ParamException(errMsg);
            }

            httpGet.setConfig(requestConfig);
            setHeaders(httpGet, httpRequest.getHeaders());
            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int http_statuscode = httpResponse.getStatusLine().getStatusCode();
                if (http_statuscode >= 500 && http_statuscode <= 599) {
                    String errMsg = String.format("http status code is %d, response body: %s", http_statuscode, getResponseString(httpResponse));
                    throw new IOException(errMsg);
                }
                if (http_statuscode != 200 && http_statuscode != 206) {
                    String responseStr = getResponseString(httpResponse);
                    String errMsg = String.format(
                            "getFileinputstream failed, httpRequest: %s, httpResponse: %s, responseStr: %s",
                            httpRequest.toString(), httpResponse.toString(), responseStr);

                    httpGet.releaseConnection();
                    throw new ServerException(errMsg);
                }
                HttpEntity entity = httpResponse.getEntity();
                COSObjectInputStream cosObjectInputStream =
                        new COSObjectInputStream(entity.getContent(), httpGet);
                return cosObjectInputStream;
            } catch (ParseException | IOException e) {
                ++retry;
                httpGet.abort();
                httpGet.releaseConnection();
                if (retry == maxRetryCount) {
                    String errMsg = getExceptionMsg(httpRequest, e.toString());
                    throw new ServerException(errMsg);
                }
            }
        }
        // never will reach here
        return null;
    }

    private String getResponseString(HttpResponse httpResponse) throws ParseException, IOException {
        String httpResponseStr = null;
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity != null) {
            httpResponseStr = EntityUtils.toString(httpEntity, "UTF-8");
        }
        
        if (httpResponseStr == null) {
            return "";
        } else {
            return httpResponseStr;
        }
    }

    private void setJsonEntity(HttpPost httpPost, Map<String, String> params) {
        ContentType utf8TextPlain = ContentType.create("text/plain", Consts.UTF_8);
        String postJsonStr = new JSONObject(params).toString();
        StringEntity stringEntity = new StringEntity(postJsonStr, utf8TextPlain);
        httpPost.setEntity(stringEntity);
    }

    private void setMultiPartEntity(HttpPost httpPost, Map<String, String> params)
            throws Exception {
        ContentType utf8TextPlain = ContentType.create("text/plain", Consts.UTF_8);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        for (String paramKey : params.keySet()) {
            if (paramKey.equals(RequestBodyKey.FILE_CONTENT)) {
                entityBuilder.addBinaryBody(RequestBodyKey.FILE_CONTENT, params
                        .get(RequestBodyKey.FILE_CONTENT).getBytes(Charset.forName("ISO-8859-1")));
            } else {
                entityBuilder.addTextBody(paramKey, params.get(paramKey), utf8TextPlain);
            }
        }
        httpPost.setEntity(entityBuilder.build());
    }

    /**
     * 设置Http头部，同时添加上公共的类型，长连接，COS SDK标识
     * 
     * @param message HTTP消息
     * @param headers 用户额外添加的HTTP头部
     */
    private void setHeaders(HttpMessage message, Map<String, String> headers) {
        message.setHeader(RequestHeaderKey.ACCEPT, RequestHeaderValue.Accept.ALL);
        message.setHeader(RequestHeaderKey.CONNECTION, RequestHeaderValue.Connection.KEEP_ALIVE);
        message.setHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());

        if (headers != null) {
            for (String headerKey : headers.keySet()) {
                message.setHeader(headerKey, headers.get(headerKey));
            }
        }
    }

}
