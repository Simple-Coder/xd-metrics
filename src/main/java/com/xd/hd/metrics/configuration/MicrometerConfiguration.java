package com.xd.hd.metrics.configuration;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.spring.autoconfigure.MeterRegistryCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dongxie on 2022/8/5.
 */
@Order(Integer.MIN_VALUE)
@Configuration
@ConditionalOnClass(MeterRegistry.class)
@Slf4j
public class MicrometerConfiguration {
    @Value("${hudongMetrics.metrics.size:1500}")
    private int metricsSize;

    @Autowired
    private List<MeterRegistry> meterRegistryList;

    @Bean
    public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String applicationName, @Autowired(required = false) MeterRegistry meterRegistry) {
        log.info("meterRegistryCustomizer appName is :{}",applicationName);
        return meterRegistry1 -> { meterRegistry.config().commonTags("application", applicationName); };
    }

    @PostConstruct
    public void init() {
        Metrics.globalRegistry.config().meterFilter(MeterThresholdFilter());
        Metrics.gauge("hudongMetrics_metrics_size", Tags.of("registry", "globalRegistry", "type", "entity"), Metrics.globalRegistry, o -> o.getMeters().size());
        Metrics.gauge("hudongMetrics_metrics_size", Tags.of("registry", "globalRegistry", "type", "doc"), Metrics.globalRegistry, o -> countMetricsSize(o.getMeters()));
        meterRegistryList.forEach(registry -> Metrics.gauge("hudongMetrics_metrics_size", Tags.of("registry", registry.getClass().getSimpleName(), "type", "entity"), registry, o -> o.getMeters().size()));
        meterRegistryList.forEach(registry -> Metrics.gauge("hudongMetrics_metrics_size", Tags.of("registry", registry.getClass().getSimpleName(), "type", "doc"), registry, o -> countMetricsSize(o.getMeters())));
        log.info("meterRegistryList size is :{}", JSONUtil.toJsonStr(meterRegistryList));
    }

    @Bean
    public MeterFilter MeterThresholdFilter() {
        return new MeterThresholdFilter(meterRegistryList);
    }

    class MeterThresholdFilter implements MeterFilter {
        private List<MeterRegistry> meterRegistryList;

        MeterThresholdFilter(List<MeterRegistry> meterRegistryList) {
            this.meterRegistryList = meterRegistryList;
        }

        @Override
        public MeterFilterReply accept(Meter.Id id) {
            int size = meterRegistryList.stream().mapToInt(meterRegistry -> meterRegistry.getMeters().size()).max().orElse(-1);
            size = Math.max(Metrics.globalRegistry.getMeters().size(), size);
            MeterFilterReply r;
            if (size < metricsSize) {
                r = MeterFilterReply.ACCEPT;
            } else {
                r = MeterFilterReply.DENY;
            }
            if (r == MeterFilterReply.DENY) {
                //会导致死锁问题 http://index.tv.sohuno.com/confluence/pages/viewpage.action?pageId=22679403
//                logger.warn("metrics size over threshold : {}, id : {}", metricsSize, id);
            }
            return r;
        }
    }

    private int countMetricsSize(List<Meter> meters) {
        AtomicInteger n = new AtomicInteger(0);
        meters.forEach(meter -> {
            if (meter instanceof Timer) {
                n.getAndAdd(3);
            } else if (meter instanceof DistributionSummary) {
                n.getAndAdd(3);
            } else if (meter instanceof FunctionTimer) {
                n.getAndAdd(2);
            } else {
                n.getAndAdd(1);
            }
        });
        return n.get();
    }
}

