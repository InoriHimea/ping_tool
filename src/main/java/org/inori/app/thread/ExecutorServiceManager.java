package org.inori.app.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ExecutorServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceManager.class);

    private static final Map<String, ExecutorService> executorServiceMap = new ConcurrentHashMap<>();
    private static final Map<String, ScheduledExecutorService> scheduledExecutorServiceMap = new ConcurrentHashMap<>();

    /**
     * CachedExecutorService
     * @param groupName
     * @return
     */
    public static ExecutorService getCachedExecutorService(String groupName) {
        ExecutorService service = Executors.newCachedThreadPool(new PingThreadFactory(groupName));

        ExecutorService target = executorServiceMap.putIfAbsent(groupName, service);
        //为null表示原来没有，否则返回已有对象
        if (target == null) {
            target = service;
        } else {
            service.shutdown();
            logger.debug("关闭{}", service.isShutdown());
        }

        return target;
    }

    /**
     * FixedExecutorService
     * @param groupName
     * @param threadSize
     * @return
     */
    public static ExecutorService getFixedExecutorService(String groupName, int threadSize) {
        ExecutorService service = Executors.newFixedThreadPool(threadSize, new PingThreadFactory(groupName));

        ExecutorService target = executorServiceMap.putIfAbsent(groupName, service);
        //为null表示原来没有，否则返回已有对象
        if (target == null) {
            target = service;
        } else {
            service.shutdown();
            logger.debug("关闭{}", service.isShutdown());
        }

        return target;
    }
}
