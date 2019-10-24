package org.inori.app.main;

import org.inori.app.model.Contacts;
import org.inori.app.model.PingModel;
import org.inori.app.thread.ExecutorServiceManager;
import org.inori.app.thread.NamedCallable;
import org.inori.app.thread.NamedRunnable;
import org.inori.app.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static final String TARGET_HOST_NAME;
    public  static final HttpClientUtils httpClient = new HttpClientUtils();

    static {
        TARGET_HOST_NAME = PropertyUtils.getProperty("host_name");
    }

    public static void main(String[] args) {
        logger.info("开始执行本次任务");

        logger.info("获取目标页面来获取需要的参数");
        String html = httpClient.get(Contacts.GET_PARAM_URL.replace("#{type}", "A")
                .replace("#{host}", TARGET_HOST_NAME));
        logger.info("获取完毕，内容为：\n{}", html);

        logger.info("开始解析页面");
        Map<String, Object> param = getApiParamByParseHtml(html);
        logger.info("参数获取完毕");

        logger.info("开始通过API解析DNS对应IPv4和IPv6");
        List<Set<String>> ips = resolvingDns(param);
        logger.info("解析完毕");

        logger.info("开始解析为对应host记录");
        ExecutorService dnsService = ExecutorServiceManager.getFixedExecutorService("DNS解析", 2);

        Vector<Future<String>> futureVector = new Vector<>(2);
        for (Set<String> ip : ips) {
            Future<String> ipFuture = dnsService.submit(new NamedCallable<String>() {

                @Override
                public String runAfter() {
                    return pingIPs2SearchMinDelay(ip);
                }
            });
            futureVector.add(ipFuture);
        }

        List<String> hostsList = new LinkedList<>();
        CountDownLatch latch = new CountDownLatch(2);

        for (Future<String> future : futureVector) {
            try {
                hostsList.add(future.get());
            } catch (InterruptedException e) {
                logger.error("中断异常", e);
            } catch (ExecutionException e) {
                logger.error("执行异常", e);
            }

            latch.countDown();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("{}", e);
        }

        logger.info("把最低延时的Hosts记录【{}】写入Hosts中", hostsList);
        FilesUtils.writeContent2File(hostsList, new File(Contacts.HOSTS_PATH), true);

        logger.info("程序执行完毕！");
        System.exit(0);
    }

    private static String pingIPs2SearchMinDelay(Set<String> ipSet) {
        List<PingModel> pingModelList = new LinkedList<>();
        ExecutorService pingService = ExecutorServiceManager.getFixedExecutorService("Ping任务", 4);

        final AtomicInteger success = new AtomicInteger(0);
        final AtomicInteger error = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(ipSet.size());
        for (String ip : ipSet) {
            pingService.submit(new NamedRunnable() {
                @Override
                public void runAfter() throws Exception {
                    try {
                        pingModelList.add(PingUtils.ping(ip, 4));
                        success.getAndIncrement();
                    } catch (IOException e) {
                        logger.error("ping[{}]执行异常", ip, e);
                        error.getAndIncrement();
                    }

                    latch.countDown();
                }
            }.setName("Ping[" + ip + "]"));
        }

        logger.info("等待所有的线程执行完毕");
        long startTime = System.currentTimeMillis();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        logger.info("总共花费了{}毫秒", endTime - startTime);

        logger.info("解析完毕，成功{}, 失败{}，总大小{}", success.get(), error.get(), ipSet.size());
        logger.info("处理后的结果集大小{}", pingModelList.size());

        logger.info("将成功的排序后去最小值！");
        logger.info("a -> {}", JacksonUtils.toDefaultPrettyJson(pingModelList));

        List<PingModel> sortedList = pingModelList.stream().sorted((o1, o2) -> {
            logger.debug("o1 -> {}", JacksonUtils.toDefaultPrettyJson(o1));
            logger.debug("o2 -> {}", JacksonUtils.toDefaultPrettyJson(o2));

            if (o1.getLostPercent() < o2.getLostPercent()) {
                return 1;
            } else if (o1.getLostPercent() > o2.getLostPercent()) {
                return -1;
            } else {
                if (o1.getAvgTime() < o2.getAvgTime()) {
                    return 1;
                } else if (o1.getAvgTime() > o2.getAvgTime()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }).collect(Collectors.toList());

        logger.info("排序后的内容：{}", JacksonUtils.toDefaultPrettyJson(sortedList));
        logger.info("最小延时为{}毫秒，最大延时为{}毫秒", sortedList.get(0), sortedList.get(sortedList.size() - 1));

        return new StringBuilder("\n")
                .append(sortedList.get(0).getIp())
                .append("\t")
                .append(TARGET_HOST_NAME).toString();
    }

    private static List<Set<String>> resolvingDns(Map<String, Object> param) {
        ExecutorService dnsService = ExecutorServiceManager.getFixedExecutorService("DNS解析", 2);

        Future<Set<String>> ipv4Set = dnsService.submit(new NamedCallable<Set<String>>() {

            @Override
            public Set<String> runAfter() throws Exception {
                return DNSUtils.resolvingDNSByAPI(param, "A");
            }
        }.setName("IPv4解析"));
        Future<Set<String>> ipv6Set = dnsService.submit(new NamedCallable<Set<String>>() {

            @Override
            public Set<String> runAfter() throws Exception {
                return DNSUtils.resolvingDNSByAPI(param, "AAAA");
            }
        }.setName("IPv6解析"));

        List<Set<String>> result = new LinkedList<>();
        try {
            Set<String> ipv4 = ipv4Set.get();
            Set<String> ipv6 = ipv6Set.get();

            result.add(ipv4);
            result.add(ipv6);
        } catch (InterruptedException e) {
            logger.error("执行中断", e);
        } catch (ExecutionException e) {
            logger.error("任务错误", e);
        }

        return result;
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
