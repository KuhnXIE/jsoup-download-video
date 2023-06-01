package com.gict.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    public ThreadPoolExecutor executor;

    public DownloadManager() {
        executor = new ThreadPoolExecutor(
                8,
                20,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10000),
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void shutdown() {
        this.executor.shutdown();
    }
}
