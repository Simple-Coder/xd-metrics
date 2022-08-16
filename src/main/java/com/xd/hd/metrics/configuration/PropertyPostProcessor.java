package com.xd.hd.metrics.configuration;

import cn.hutool.core.util.StrUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * Created by dongxie on 2022/8/5.
 */
@Deprecated
public class PropertyPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties properties = new Properties();

        //prometheus eureka服务发现
        properties.put("eureka.instance.metadata-map.prometheus.scrape", "true");
        properties.put("eureka.instance.metadata-map.prometheus.path", "/prometheus");
//        properties.put("eureka.instance.metadata-map.prometheus.port", "${server.port}");
        properties.put("eureka.instance.instance-id", "${spring.cloud.client.ipAddress}:${server.port}");
        properties.put("eureka.instance.prefer-ip-address", "true");

        String managementPort = environment.getProperty("management.port");
        if (StrUtil.isNotBlank(managementPort)) {
            System.out.println("management.port is" + managementPort + ",not use custom");
            properties.put("eureka.instance.metadata-map.prometheus.port", managementPort);
        } else {
            System.out.println("management.port is empty,will use custom");
            properties.put("management.port", "${server.port}");
            properties.put("eureka.instance.metadata-map.prometheus.port", "${server.port}");
        }
        properties.put("endpoints.prometheus.enabled", true);
        properties.put("management.security.enabled", false);

        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("hudongMetrics", properties);
        environment.getPropertySources().addLast(propertiesPropertySource);

//        properties.put("management.endpoint.health.show-details", "always");
//        properties.put("management.endpoint.prometheus.enabled", true);
//        properties.put("management.endpoint.jolokia.enabled", false);
//        properties.put("management.endpoint.env.post.enabled", false);
//        properties.put("management.metrics.web.client.request.autotime.enabled", false);
//        properties.put("spring.autoconfigure.exclude", "org.springframework.boot.actuate.autoconfigure.metrics.web.client.HttpClientMetricsAutoConfiguration");
//        properties.put("management.endpoints.web.exposure.include", "health,info,prometheus");

//        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("hudongMetrics", properties);
//        environment.getPropertySources().addLast(propertiesPropertySource);
    }
}
