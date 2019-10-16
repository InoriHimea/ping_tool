package org.inori.app;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSTest {

    private static final Logger logger = LoggerFactory.getLogger(DNSTest.class);

    @Test
    public void testDNS() {

        try {
            InetAddress[] allByName = InetAddress.getAllByName("www.taobao.com");
            for (InetAddress inetAddress : allByName) {
                logger.info(inetAddress.getHostAddress());
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
