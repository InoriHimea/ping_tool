package org.inori.app.model;

public class Contacts {

    /*
        最大连接数
     */
    public static final int HTTP_CLIENT_MAX_TOTAL = 200;
    /*
        每个路由的基础连接数
     */
    public static final int HTTP_CLIENT_CONNECT_ROUTE = 20;
    /*
        设置复用连接时间
     */
    public static final int HTTP_CLIENT_VALIDATE_TIME = 30000;
    /*
        Socket链接超时时间
     */
    public static final int HTTP_CLIENT_SOCKET_TIMEOUT = 10000;
    /*
        链接超时时间
     */
    public static final int HTTP_CLIENT_CONNECT_TIMEOUT = 5000;
    /*
        请求超时时间
     */
    public static final int HTTP_CLIENT_REQUEST_CONNECT_TIMEOUT = 3000;

    /**
     * 浏览器AGENT
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36";
}
