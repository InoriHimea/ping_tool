package org.inori.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;

public final class IPUtils {

    private static final Logger logger = LoggerFactory.getLogger(IPUtils.class);

    public static boolean isIPv6(String ip) {
        return ip.contains(":");
    }
}
