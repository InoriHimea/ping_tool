package org.inori.app.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.inori.app.model.Contacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class HttpClientUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private static HttpClientUtils httpClient;
    private static PoolingHttpClientConnectionManager manager;
    private static CloseableHttpClient client;

   static {
       ConnectionSocketFactory socketFactory = PlainConnectionSocketFactory.getSocketFactory();
       LayeredConnectionSocketFactory sslFactory = createSSLConnectionSocketFactory();

       Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", socketFactory)
               .register("https", sslFactory)
               .build();

       manager = new PoolingHttpClientConnectionManager(registry);
       manager.setMaxTotal(Contacts.HTTP_CLIENT_MAX_TOTAL);
       manager.setDefaultMaxPerRoute(Contacts.HTTP_CLIENT_CONNECT_ROUTE);
       manager.setValidateAfterInactivity(Contacts.HTTP_CLIENT_VALIDATE_TIME);
       manager.setDefaultSocketConfig(SocketConfig.custom()
               .setSoTimeout(Contacts.HTTP_CLIENT_SOCKET_TIMEOUT)
               .build());

       RequestConfig requestConfig = RequestConfig.custom()
               .setConnectTimeout(Contacts.HTTP_CLIENT_CONNECT_TIMEOUT)
               .setConnectionRequestTimeout(Contacts.HTTP_CLIENT_REQUEST_CONNECT_TIMEOUT)
               .setSocketTimeout(Contacts.HTTP_CLIENT_SOCKET_TIMEOUT)
               .build();

       client = HttpClients.custom()
               .setConnectionManager(manager)
               .setDefaultRequestConfig(requestConfig)
               .setRetryHandler((exception, executionCount, context) -> {
                   if (executionCount >= 3) {
                       logger.error("尝试达到或超过3次无应答", exception);
                       return false;
                   }
                   if (exception instanceof NoHttpResponseException) {
                       logger.warn("无响应，尝试重连", exception);
                       return true;
                   }
                   if (exception instanceof SSLHandshakeException) {
                       logger.error("SSL handshake is error", exception);
                        return false;
                   }
                   if (exception instanceof InterruptedIOException) {
                       logger.error("I/O异常中断", exception);
                        return false;
                   }
                   if (exception instanceof UnknownHostException) {
                        logger.error("未知的主机异常", exception);
                       return false;
                   }
                   if (exception instanceof ConnectTimeoutException) {
                        logger.warn("本次链接超时，{}", exception.getMessage(), exception);
                        return true;
                   }
                   if (exception instanceof SSLException) {
                       logger.error("SSL is error", exception);
                       return false;
                   }

                   HttpClientContext adapt = HttpClientContext.adapt(context);
                   HttpRequest request = adapt.getRequest();
                   if (! (request instanceof HttpEntityEnclosingRequest)) {
                       logger.debug("当前请求代用实体");
                        return true;
                   }

                   return false;
               })
               .build();
   }

   private static SSLConnectionSocketFactory createSSLConnectionSocketFactory() {
       SSLContext sslContext;
       SSLConnectionSocketFactory sslConnectionSocketFactory;

       try {
           sslContext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);

           sslContext.init(null, new TrustManager[]{new X509TrustManager() {
               @Override
               public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

               }

               @Override
               public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

               }

               @Override
               public X509Certificate[] getAcceptedIssuers() {
                   return new X509Certificate[0];
               }
           }}, null);

           sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
       } catch (NoSuchAlgorithmException | KeyManagementException e) {
           logger.error("创建失败,{}", e.getMessage(), e);
           sslConnectionSocketFactory = null;
       }

       return sslConnectionSocketFactory;
   }

    /**
     * 发送get请求
     * @param url
     */
    public String get(String url) {
        HttpGet get = new HttpGet(url);

        get.addHeader("Host", url.substring(url.indexOf("//") + 2, url.indexOf("/", url.indexOf("//") + 2)));
        get.addHeader("User-Agent", Contacts.USER_AGENT);
        get.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        get.addHeader("Accept-Language", "zh-TW,zh;q=0.8,zh-HK;q=0.6,en-US;q=0.4,en;q=0.2");
        get.addHeader("Connection", "keep-alive");
        get.addHeader("Upgrade-Insecure-Requests", "1");
        get.addHeader("Pragma", "no-cache");
        get.addHeader("Cache-Control", "no-cache");
        get.addHeader("TE", "Trailers");

        return execute(get);
    }

    private String execute(HttpRequestBase request) {
        HttpClientContext context = HttpClientContext.create();
        CloseableHttpResponse response = null;
        String content = "error";

        try {
            response = client.execute(request, context);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                Charset charset = ContentType.getOrDefault(entity).getCharset();
                content = EntityUtils.toString(entity, charset);
                EntityUtils.consume(entity);
            }
        } catch (IOException e) {
            logger.error("{}", e.getMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("关闭响应异常, {}", e.getMessage(), e);
            }

            if (request != null) {
                request.releaseConnection();
            }
        }

        return content;
    }

    /**
     * 获取连接池中的存在的链接数
     * @return
     */
   public static String getTotalConnectionNum() {
       return manager != null ? JacksonUtils.toJson(manager.getTotalStats()) : "-1";
   }
}
