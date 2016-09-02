package com.example.smy.photoloading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SMY on 2016/9/2.
 */
public class CustomThreadPoolFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    public CustomThreadPoolFactory(String poolName){
        namePrefix = poolName;
    }
    public Thread newThread(Runnable r) {
        return new Thread(r, namePrefix +" #" + threadNumber.getAndIncrement());
    }
}
