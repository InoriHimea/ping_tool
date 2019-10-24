package org.inori.app.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public abstract class NamedCallable<V> implements Callable<V> {

    private static final Logger logger = LoggerFactory.getLogger(NamedCallable.class);

    private String name;
    private boolean throwException = false;

    @Override
    public V call() {
        NamedRunnable.renameThreadName(name, logger);

        try {
            return runAfter();
        } catch (Exception e) {
            logger.error("当前线程{}出现异常：", Thread.currentThread().getName(), e.getMessage(), e);

            if (throwException) {
                throw new RuntimeException("当前指定为执行异常后不继续后续内容");
            }
        }

        return null;
    }

    public abstract V runAfter() throws Exception;

    public NamedCallable<V> setName(String name) {
        this.name = name;
        return this;
    }

    public NamedCallable<V> setThrowException(boolean throwException) {
        this.throwException = throwException;
        return this;
    }

}
