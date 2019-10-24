package org.inori.app.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 可自定义线程名称的类
 */
public abstract class NamedRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NamedRunnable.class);

    private String name;
    private boolean throwException = false;

    @Override
    public void run() {
        renameThreadName(name, logger);

        try {
            runAfter();
        } catch (Exception e) {
            logger.error("当前线程{}出现异常：", Thread.currentThread().getName(), e.getMessage(), e);

            if (throwException) {
                throw new RuntimeException("当前指定为执行异常后不继续后续内容");
            }
        }
    }

    static void renameThreadName(String name, Logger logger) {
        String oldName = Thread.currentThread().getName();
        String prefix = oldName.substring(0, oldName.indexOf("-", oldName.indexOf("-") + 1) + 1);
        String suffix = oldName.substring(oldName.lastIndexOf("-"));
        String newName = prefix + name + suffix;
        Thread.currentThread().setName(newName);
        logger.info("当前的线程为【{} -> {}】", oldName, newName );
    }

    public abstract void runAfter() throws Exception;

    public NamedRunnable setName(String name) {
        this.name = name;
        return this;
    }

    public NamedRunnable setThrowException(boolean throwException) {
        this.throwException = throwException;
        return this;
    }
}
