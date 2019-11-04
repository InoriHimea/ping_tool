package org.inori.app.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

@Slf4j
public class PingForkJoinWorkerThread extends ForkJoinWorkerThread {

    /**
     * Creates a ForkJoinWorkerThread operating in the given pool.
     *
     * @param pool the pool this thread works in
     * @throws NullPointerException if pool is null
     */
    protected PingForkJoinWorkerThread(ForkJoinPool pool, String groupName) {
        super(pool);
        log.debug("原始名称【{}】", this.getName());
        String threadName = this.getName().replace("ForkJoinPool", "ForkJoinPool_" + groupName);
        log.debug("更新名称【{}】", threadName);
        this.setName(threadName);
    }
}
