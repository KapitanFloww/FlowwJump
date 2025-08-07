package com.github.kapitanfloww.jump.util;

public class Timer {

    private long startingTime = 0L;

    public Timer() {
        this.start();
    }

    public void start() {
        this.startingTime = System.currentTimeMillis();
    }

    public long stop() {
        return (System.currentTimeMillis() - startingTime) / 1000;
    }

    public long getTime() {
        return (System.currentTimeMillis() - startingTime) / 1000;
    }
}
