package com.xd.hd.metrics.exceptions;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dongxie on 2022/8/5.
 */
@Slf4j
public class ExceptionsMetric {
    private MeterRegistry meterRegistry;
    private final String name;

    private final ConcurrentHashMap<String, Counter> exceptionCounters = new ConcurrentHashMap<>();

    public ExceptionsMetric(String name, MeterRegistry meterRegistry) {
        this.name = name;
        this.meterRegistry = meterRegistry;
    }

    public void count(Throwable ex) {
        getOrCreateCounter(extractName(ex)).increment();
    }

    public void count(String exceptionName) {
        getOrCreateCounter(exceptionName).increment();
    }

    @PreDestroy
    public void shutdown() {
        for (Map.Entry<String, Counter> entry : exceptionCounters.entrySet()) {
            this.meterRegistry.remove(entry.getValue());
        }
    }

    private Counter getOrCreateCounter(String exceptionName) {
        Counter counter = exceptionCounters.get(exceptionName);
        if (Objects.isNull(counter)) {
            counter = Counter.builder(name).tags("id",exceptionName).description(exceptionName).register(this.meterRegistry);
            exceptionCounters.put(exceptionName, counter);
        }
        return counter;
    }

    private static String extractName(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getClass().getSimpleName();
    }
}
