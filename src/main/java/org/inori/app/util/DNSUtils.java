package org.inori.app.util;

import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Transformer;
import org.inori.app.main.MainApp;
import org.inori.app.model.Contacts;
import org.inori.app.thread.ExecutorServiceManager;
import org.inori.app.thread.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class DNSUtils {

    private static final Logger logger = LoggerFactory.getLogger(DNSUtils.class);
    private static final HttpClientUtils httpClient = new HttpClientUtils();

    public static Set<String> resolvingDNSByAPI(Map<String, Object> param, String type) {
        String typeName = type.equals("A") ? "IPv4" : "IPv6";
        String url = Contacts.DEFAULT_API.replace("#{type}", type)
                .replace("#{host}", MainApp.TARGET_HOST_NAME)
                .replace("#{token}", String.valueOf(param.get("token")));

        @SuppressWarnings("unchecked")
        List<String> value = (List<String>) param.get("dnsId");

        Set<String> ipSet = new LinkedHashSet<>();
        ExecutorService APIService = ExecutorServiceManager.getFixedExecutorService(typeName + "API解析", 4);
        CountDownLatch latch = new CountDownLatch(value.size());

        for (String id : value) {
            APIService.submit(new NamedRunnable() {

                @Override
                public void runAfter() {
                    String tempUrl = url.replace("#{serverId}", id);

                    logger.info("url[{}]为本次访问地址", tempUrl);
                    String result = httpClient.get(tempUrl);
                    logger.info("本次请求到的结果{}", result);
                    ipSet.addAll(Arrays.stream(result.split("<br />")).filter(string -> ! string.isEmpty() && ! string.equals("error")).collect(Collectors.toSet()));
                    latch.countDown();
                }
            }.setName(typeName + "[" + id + "]请求"));
        }

        logger.info("等待所有{}的解析完成", typeName);
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("解析中断异常", e);
        }

        logger.info("通过API解析{}的地址完成", typeName);
        return ipSet;
    }

    public static Set<String> queryIPByDNS(String dnsHost, String type) {
        Set<String> ipSet = new LinkedHashSet<>();

        System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        System.setProperty("sun.net.spi.nameservice.nameservers", dnsHost);

        try {
            InetAddress[] allIP = InetAddress.getAllByName(MainApp.TARGET_HOST_NAME);
            List<InetAddress> ipList = Arrays.asList(allIP);

            switch (type) {
                case "A":
                    ipSet.addAll(ipList.stream()
                            .map(InetAddress::getHostAddress)
                            .filter(ip -> ! IPUtils.isIPv6(ip))
                            .collect(Collectors.toSet()));
                    break;
                case "AAAA":
                    ipSet.addAll(ipList.stream()
                            .map(InetAddress::getHostAddress)
                            .filter(ip -> IPUtils.isIPv6(ip))
                            .collect(Collectors.toSet()));
                    break;
            }
        } catch (UnknownHostException e) {
            logger.error("未知的主机异常", e);
        }

        return ipSet;
    }
}
