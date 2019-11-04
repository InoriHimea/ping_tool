package org.inori.app.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

@Slf4j
public class PingForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

    private final String groupName;

    public PingForkJoinWorkerThreadFactory(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Returns a new worker thread operating in the given pool.
     *
     * @param pool the pool this thread works in
     * @return the new worker thread
     * @throws NullPointerException if the pool is null
     */
    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        log.debug("开始创建一个ForkJoinWorker线程");
        return new PingForkJoinWorkerThread(pool, groupName);
    }
}
