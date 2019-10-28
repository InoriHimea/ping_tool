package org.inori.app.main;

import org.inori.app.model.Contacts;
import org.inori.app.thread.ExecutorServiceManager;
import org.inori.app.thread.NamedCallable;
import org.inori.app.util.DNSUtils;
import org.inori.app.util.HttpClientUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class GlobalDNS {

    private static final Logger logger = LoggerFactory.getLogger(GlobalDNS.class);

    private static GlobalDNS instance;
    private static final HttpClientUtils httpClient = new HttpClientUtils();

    public static GlobalDNS getInstance() {
        if (instance == null) {
            instance = new GlobalDNS();
        }
        return instance;
    }

    public Set<String> queryIPList(Map<String, Object> param, String type) {
        logger.debug("根据参数和类型通过API获取对应的IP集");
        Set<String> ipSet = this.resolvingDns(type, param);
        logger.debug("通过API解析完毕");
        return ipSet;
    }

    public Map<String, Object> getParam(String type) {
        logger.debug("开始获取{}类型的参数", type);
        String html = this.getHtml(type);
        logger.debug("当前或得到的页面：{}", html);

        logger.debug("开始解析页面");
        Map<String, Object> param = this.getApiParamByParseHtml(html);
        logger.debug("参数获取完毕");
        return param;
    }

    public String getHtml(String type) {

        return httpClient.get(Contacts.GET_PARAM_URL.replace("#{type}", type)
                .replace("#{host}", MainApp.TARGET_HOST_NAME));
    }

    /**
     * 通过Jsoup解析页面获取参数
     * @return
     */
    public Map<String, Object> getApiParamByParseHtml(String html) {
        Map<String, Object> param = new LinkedHashMap<>();
        List<String> dnsIdList = new LinkedList<>();

        Document document = Jsoup.parse(html);
        logger.debug("页面内容：\n{}", document.toString());
        if (document == null) {
            logger.warn("解析页面失败，可能未获得正确内容");
            System.exit(0);
        }

        String token = document.getElementById("_token").val();
        logger.debug("本次获得的token为{}", token);

        Elements tr = document.getElementsByTag("tr");
        for (Element element : tr) {
            String dnsId = element.attr("data-id");
            logger.debug("本次的DNS_ID -> {}", dnsId);
            dnsIdList.add(dnsId);
        }

        param.put("token", token);
        param.put("dnsId", dnsIdList);

        return param;
    }

    public Set<String> resolvingDns(final String type, final Map<String, Object> param) {
        ExecutorService dnsService = ExecutorServiceManager.getFixedExecutorService("DNS解析", 2);

        Future<Set<String>> ipSet = dnsService.submit(new NamedCallable<Set<String>>() {

            @Override
            public Set<String> runAfter() throws Exception {
                return DNSUtils.resolvingDNSByAPI(param, type);
            }
        }.setName(type.equals("A") ? "IPv4" : "IPv6" + "解析"));

        try {
            return ipSet.get();
        } catch (InterruptedException e) {
            logger.error("执行中断", e);
        } catch (ExecutionException e) {
            logger.error("任务错误", e);
        }

        return null;
    }
}
