package org.inori.app;

import org.inori.app.main.MainApp;
import org.inori.app.model.PingModel;
import org.inori.app.util.DNSUtils;
import org.inori.app.util.JacksonUtils;
import org.inori.app.util.PingUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DNSTest {

    private static final Logger logger = LoggerFactory.getLogger(DNSTest.class);

    @Test
    void testDNS() throws UnknownHostException {
        /*Set<String> ipSet = DNSUtils.queryIPByDNS("210.6.157.52", "A");
        ipSet.forEach(System.out::println);*/
        InetAddress[] allByName = InetAddress.getAllByName(MainApp.TARGET_HOST_NAME);
        for (InetAddress inetAddress : allByName) {
            System.out.println(inetAddress.getHostAddress());
        }
    }

    @Test
    void testMath() {
        Map<String, Boolean> map = new ConcurrentHashMap<>();
        Set<String> strings = Collections.newSetFromMap(map);

        int i = 0;
        while (i < 100) {
            strings.add(i + "");
            i ++;
            logger.info("{}", i);
        }

        logger.info("{}", map);
        logger.info("{}", strings);
    }

    @Test
    void pingTest() {
        CountDownLatch latch = new CountDownLatch(5);

        try {
            PingModel ping = PingUtils.ping("2407:8800:bf00:609d::180c", 4);
            logger.info("ping -> {}", JacksonUtils.toDefaultPrettyJson(ping));
            latch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
