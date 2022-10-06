package com.training.apparatus.data.widget;

public class Stopwatch {
    private long timer_s;
    private long timer_r;
    private long timer_f;

    private boolean start = false;

    public Stopwatch() {

    }

    public void start() {
        if (!start) {
            timer_s = System.currentTimeMillis();
            timer_f = timer_s;
            start = true;
        }
    }

    public void stop() {
        if (start) {
            timer_f = System.currentTimeMillis();
            timer_r = timer_f - timer_s;
            start = false;
        }
    }

    public double getResultSec() {
        return (double)timer_r / 1000;
    }

    public double  getResultMin() {
        return (double)timer_r / 1000 / 60;
    }

    public double getCurrentTimeSec() {
        if (start) {
            return (System.currentTimeMillis() - timer_s)/1000.0;
        } else {
            return 0.0;
        }
    }

    public boolean isStarted() {
        return start;
    }
}
