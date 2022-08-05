package com.xd.hd.metrics.exceptions;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcChildContextConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by dongxie on 2022/8/5.
 */
@ConditionalOnJava(value = ConditionalOnJava.JavaVersion.EIGHT)
@Configuration
@ConditionalOnClass(name = {"ch.qos.logback.core.AppenderBase",
        "org.springframework.boot.actuate.metrics.export.Exporter",
        "io.micrometer.core.instrument.MeterRegistry"})
@AutoConfigureBefore(EndpointWebMvcChildContextConfiguration.class)
@Slf4j
public class ExceptionMetricsAutoConfiguration {
    private String metricName = "logback.exception";

    @Resource(name = "exceptionStatsAppender")
    private Appender<ILoggingEvent> appender;

    @PostConstruct
    public void init() {
        if (LoggerFactory.getILoggerFactory() != null && LoggerFactory.getILoggerFactory() instanceof LoggerContext) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            appender.setContext(loggerContext);
            appender.start();
            List<Logger> loggers = loggerContext.getLoggerList();
            for (Logger logger : loggers) {
                String name = logger.getName();
                if (Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(name)) {
                    logger.addAppender(appender);
                    logger.info("logger={} add metric Done", logger.getName());
                } else if (!logger.isAdditive()) {
                    // 非传递性logger单独传递appender
                    logger.addAppender(appender);
                    logger.info("logger={} add metric Done", logger.getName());
                }
            }
        } else {
            log.error("loggerExceptionsMetric init failed , LoggerFactory.getILoggerFactory()={}",
                    LoggerFactory.getILoggerFactory());
        }
    }

    @Bean
    @ConditionalOnMissingBean(name = "exceptionStatsAppender")
    public Appender<ILoggingEvent> exceptionStatsAppender(@Autowired(required = false) MeterRegistry meterRegistry) {
        ExceptionStatsAppender appender = new ExceptionStatsAppender(new ExceptionsMetric(metricName,meterRegistry));
        return appender;
    }
}
