package org.inori.app.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PingThreadFactory implements ThreadFactory {

    private static final Logger logger = LoggerFactory.getLogger(PingThreadFactory.class);

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String prefixName;

    public PingThreadFactory(String groupName) {
        this.group = new ThreadGroup(getMainThreadGroup(), groupName);
        this.prefixName = ((groupName == null || groupName.equals("")) ?
                 Thread.currentThread().getThreadGroup().getName() : groupName) +
                  "-" + poolNumber.getAndIncrement();
    }

    private ThreadGroup getMainThreadGroup() {
        ThreadGroup targetGroup = Thread.currentThread().getThreadGroup();

        while (! targetGroup.getName().equals("main")) {
            targetGroup = targetGroup.getParent();
        }

        return targetGroup;
    }

    /**
     * Constructs a new {@code Thread}.  Implementations may also initialize
     * priority, name, daemon status, {@code ThreadGroup}, etc.
     *
     * @param r a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     * create a thread is rejected
     */
    @Override
    public Thread newThread(Runnable r) {
        logger.debug("开始创建{}组的{}线程", group.getName(), Thread.currentThread().getName());

        Thread thread = new Thread(group, r, prefixName +
                "-thread-" + threadNumber.getAndIncrement());

        if (thread.isDaemon()) {
            thread.setDaemon(true);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }

        return thread;
    }
}
