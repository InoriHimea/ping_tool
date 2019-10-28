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
    public static final int IP_SEARCH_TIMES;

    static {
        TARGET_HOST_NAME = PropertyUtils.getProperty("host_name");
        IP_SEARCH_TIMES = Integer.parseInt(PropertyUtils.getProperty("ip_search_times"));
    }

    public static void main(String[] args) {
        logger.info("开始执行本次任务");

        logger.info("开始获取Host对应的IP");
        ExecutorService hostQuery = ExecutorServiceManager.getCachedExecutorService("Host解析组");
        GlobalDNS globalDNS = GlobalDNS.getInstance();

        final Set<String> ipv4Set = new LinkedHashSet<>();
        final Set<String> ipv6Set = new LinkedHashSet<>();
        for (int i = 1; i <= IP_SEARCH_TIMES; i++) {
            logger.info("开始执行第{}次的{}解析", i, TARGET_HOST_NAME);
            CountDownLatch latch = new CountDownLatch(2);
            final Map<String, Object> param = globalDNS.getParam("A");

            hostQuery.submit(new NamedRunnable() {
                @Override
                public void runAfter() {
                    ipv4Set.addAll(globalDNS.queryIPList(param, "A"));
                    latch.countDown();
                }
            }.setName("IPv4处理"));

            hostQuery.submit(new NamedRunnable() {
                @Override
                public void runAfter() {
                    ipv6Set.addAll(globalDNS.queryIPList(param, "AAAA"));
                    latch.countDown();
                }
            }.setName("IPv6处理"));

            try {
                logger.info("等待第{}轮任务的执行完毕", i);
                latch.await();
                logger.info("执行完成,本次的结果 -> IPv4有{}个，IPv6有{}个", ipv4Set.size(), ipv6Set.size());
            } catch (InterruptedException e) {
                logger.error("错误", e);
            }
        }

        logger.info("获取最小延时组合");
        Vector<String> lowDelayIPs = IPUtils.getLowDelayIP(ipv4Set, ipv6Set);
        logger.info("获取完毕");

        logger.info("写入Host");
        FilesUtils.writeContent2File(lowDelayIPs, new File(Contacts.HOSTS_PATH), true);
        logger.info("写入完成");

        logger.info("程序执行完毕！");
        System.exit(0);
    }
}
