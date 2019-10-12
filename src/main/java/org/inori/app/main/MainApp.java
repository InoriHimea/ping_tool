package org.inori.app.main;

import org.inori.app.util.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.BasicMarker;
import org.slf4j.helpers.BasicMarkerFactory;

public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        logger.info("开始执行本次任务");
        HttpClientUtils client = new HttpClientUtils();
    }
}
