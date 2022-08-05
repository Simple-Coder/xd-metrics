package com.xd.hd.metrics.exceptions;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;

/**
 * Created by dongxie on 2022/8/5.
 */
public class ExceptionStatsAppender extends AppenderBase<ILoggingEvent> {

    private final ExceptionsMetric exceptionsMetric;

    public ExceptionStatsAppender(ExceptionsMetric exceptionsMetric) {
        this.exceptionsMetric = exceptionsMetric;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event == null) {
            return;
        }
        if (event.getLevel() == Level.ERROR || event.getLevel() == Level.WARN) {
            ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
            if (throwableProxy != null && throwableProxy.getThrowable() != null) {
                String exceptionName = throwableProxy.getThrowable().getClass().getSimpleName();
                exceptionsMetric.count(exceptionName);
            }
        }
    }

}
