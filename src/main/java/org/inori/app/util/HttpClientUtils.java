package org.inori.app.util;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.inori.app.model.Contacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class HttpClientUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private static PoolingHttpClientConnectionManager manager = null;
    private static CloseableHttpClient client = null;

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
           sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
       } catch (NoSuchAlgorithmException e) {
           logger.error("创建失败,{}", e.getMessage(), e);
           sslConnectionSocketFactory = null;
       }

       return sslConnectionSocketFactory;
   }

    /**
     * 发送post请求
     * @param url
     * @param params
     */
    public void post(String url, Map<String, Object> params) {
        HttpPost post = new HttpPost(url);

    }

    /**
     * 发送post请求，自定义文件头
     * @param url
     * @param params
     * @param header
     */
   public void post(String url, Map<String, Object> params, Map<String, Object> header) {

   }

    /**
     * 获取连接池中的存在的链接数
     * @return
     */
   public static String getTotalConnectionNum() {
       return manager != null ? JacksonUtils.toJson(manager.getTotalStats()) : "-1";
   }
}
