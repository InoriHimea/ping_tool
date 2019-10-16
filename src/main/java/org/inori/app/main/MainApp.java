package org.inori.app.main;

import org.inori.app.model.Contacts;
import org.inori.app.util.HttpClientUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.BasicMarker;
import org.slf4j.helpers.BasicMarkerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        logger.info("开始执行本次任务");

        HttpClientUtils httpClient = new HttpClientUtils();
        String totalConnectionNum = HttpClientUtils.getTotalConnectionNum();
        logger.info("aaa:" + totalConnectionNum);

        logger.info("获取目标页面来获取需要的参数");
        String html = httpClient.get(Contacts.GET_PARAM_URL.replace("#{type}", "A")
                .replace("#{host}", "www.qq.com"));
        logger.info("获取完毕，内容为：\n{}", html);

        logger.info("开始解析页面");
        Map<String, Object> param = getApiParamByParseHtml(html);
        logger.info("参数获取完毕");

        logger.info("开始通过API解析DNS对应IPv4和IPv6");
        String url = Contacts.DEFAULT_API.replace("#{type}", "A")
                .replace("#{host}", "www.qq.com")
                .replace("#{token}", String.valueOf(param.get("token")));

        List<String> value = (List<String>) param.get("dnsId");

        for (String id : value) {
            url = url.replace("#{serverId}", id);

            logger.info("url[{}]为本次访问地址", url);
            String result = httpClient.get(url);
            logger.info("本次请求到的结果{}", result);
        }

        totalConnectionNum = HttpClientUtils.getTotalConnectionNum();
        logger.info("aaa:" + totalConnectionNum);
    }

    /**
     * 通过Jsoup解析页面获取参数
     * @return
     */
    private static Map<String, Object> getApiParamByParseHtml(String html) {
        Map<String, Object> param = new LinkedHashMap<>();
        List<String> dnsIdList = new LinkedList<>();

        Document document = Jsoup.parse(html);
        logger.debug("页面内容：\n{}", document.toString());

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
}
