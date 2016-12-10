package com.qcloud.cos.http;

import java.io.InputStream;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.ParamException;

public abstract class AbstractCosHttpClient {
    protected ClientConfig config;
    protected HttpClient httpClient;

    protected PoolingHttpClientConnectionManager connectionManager;
    protected IdleConnectionMonitorThread idleConnectionMonitor;

    protected RequestConfig requestConfig;

    public AbstractCosHttpClient(ClientConfig config) {
        super();
        this.config = config;
        this.connectionManager = new PoolingHttpClientConnectionManager();
        this.connectionManager.setMaxTotal(config.getMaxConnectionsCount());
        this.connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsCount());
        HttpClientBuilder httpClientBuilder =
                HttpClients.custom().setConnectionManager(connectionManager);
        if (config.getHttpProxyIp() != null && config.getHttpProxyPort() != 0) {
            HttpHost proxy = new HttpHost(config.getHttpProxyIp(), config.getHttpProxyPort());
            httpClientBuilder.setProxy(proxy);
        }
        this.httpClient = httpClientBuilder.build();
        this.requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(this.config.getConnectionRequestTimeout())
                .setConnectTimeout(this.config.getConnectionTimeout())
                .setSocketTimeout(this.config.getSocketTimeout()).build();
        this.idleConnectionMonitor = new IdleConnectionMonitorThread(this.connectionManager);
        this.idleConnectionMonitor.start();
    }

    protected abstract String sendPostRequest(HttpRequest httpRequest) throws AbstractCosException;

    protected abstract String sendGetRequest(HttpRequest httpRequest) throws AbstractCosException;

    public String sendHttpRequest(HttpRequest httpRequest) throws AbstractCosException {

        HttpMethod method = httpRequest.getMethod();
        if (method == HttpMethod.POST) {
            return sendPostRequest(httpRequest);
        } else if (method == HttpMethod.GET) {
            return sendGetRequest(httpRequest);
        } else {
            throw new ParamException("Unsupported Http Method");
        }
    }

    public abstract InputStream getFileInputStream(HttpRequest httpRequest)
            throws AbstractCosException;

    public void shutdown() {
        this.idleConnectionMonitor.shutdown();
    }
}
