package org.inori.app.util;

import org.inori.app.main.MainApp;
import org.inori.app.model.PingModel;
import org.inori.app.thread.ExecutorServiceManager;
import org.inori.app.thread.NamedCallable;
import org.inori.app.thread.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet6Address;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class IPUtils {

    private static final Logger logger = LoggerFactory.getLogger(IPUtils.class);

    public static boolean isIPv6(String ip) {
        return ip.contains(":");
    }

    public static Vector<String> getLowDelayIP(final Set<String> ipv4, final Set<String> ipv6) {
        ExecutorService ipDelay = ExecutorServiceManager.getFixedExecutorService("IP延时筛选组", 2);

        Future<String> ipv4Relay = ipDelay.submit(new NamedCallable<String>() {

            @Override
            public String runAfter() {
                return pingIPs2SearchMinDelay(ipv4);
            }
        }.setName("IPv4延时解析"));
        Future<String> ipv6Relay = ipDelay.submit(new NamedCallable<String>() {

            @Override
            public String runAfter() {
                return pingIPs2SearchMinDelay(ipv6);
            }
        }.setName("IPv6延时解析"));

        Vector<String> hosts = new Vector<>(2);
        try {
            String ipv4Host = ipv4Relay.get();
            String ipv6Host = ipv6Relay.get();

            if (ipv4Host != null && ! ipv4Host.equals("")) {
                hosts.add(ipv4Host);
            }
            if (ipv6Host != null && ! ipv6Host.equals("")) {
                hosts.add(ipv6Host);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return hosts;
    }

    private static String pingIPs2SearchMinDelay(Set<String> ipSet) {
        final List<PingModel> pingModelList = new LinkedList<>();
        ExecutorService pingService = ExecutorServiceManager.getFixedExecutorService("Ping任务", 4);

        final AtomicInteger success = new AtomicInteger(0);
        final AtomicInteger error = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(ipSet.size());
        for (String ip : ipSet) {
            pingService.submit(new NamedRunnable() {
                @Override
                public void runAfter() throws Exception {
                    try {
                        PingModel ping = PingUtils.ping(ip, 4);
                        if (ping != null) {
                            pingModelList.add(ping);
                            success.getAndIncrement();
                        } else {
                            error.getAndIncrement();
                        }
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

        if (pingModelList.size() == 0) {
            return null;
        }

        logger.info("将成功的排序后去最小值！");
        logger.info("结果 -> {}", JacksonUtils.toDefaultPrettyJson(pingModelList));

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
                .append(MainApp.TARGET_HOST_NAME).toString();
    }
}
