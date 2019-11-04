package org.inori.app.util;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TimeCounter {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss.SSS", Locale.CHINESE);

    private long startTime;
    private long endTime;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private String message;

    public TimeCounter initStart(TimeUnit unit) {
        this.startTime = System.currentTimeMillis();
        this.unit = unit;
        this.message = message;
        return this;
    }

    public TimeCounter startTimeCount() {
        log.info("执行开始时间【{}】", sdf.format(this.startTime));
        return this;
    }

    public TimeCounter initEnd(String message) {
        this.endTime = System.currentTimeMillis();
        this.message = message;
        return this;
    }

    public TimeCounter endTimeCount() {
        log.info("执行结束时间【{}】", sdf.format(this.endTime));
        log.info("{}{}", this.message, TimeUnit.valueOf(unit.name()).convert(this.endTime - this.startTime, this.unit));
        return this;
    }

    @Override
    public String toString() {
        return "TimeCounter{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", unit=" + unit +
                ", message='" + message + '\'' +
                '}';
    }
}
