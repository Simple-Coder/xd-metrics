package com.xd.hd.metrics.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * Created by dongxie on 2022/8/5.
 */
public class PropertyPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties properties = new Properties();

        properties.put("endpoints.prometheus.enabled", true);

//        properties.put("management.endpoint.health.show-details", "always");
//        properties.put("management.endpoint.prometheus.enabled", true);
//        properties.put("management.endpoint.jolokia.enabled", false);
//        properties.put("management.endpoint.env.post.enabled", false);
//        properties.put("management.metrics.web.client.request.autotime.enabled", false);
//        properties.put("spring.autoconfigure.exclude", "org.springframework.boot.actuate.autoconfigure.metrics.web.client.HttpClientMetricsAutoConfiguration");
//        properties.put("management.endpoints.web.exposure.include", "health,info,prometheus");

        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("hudongMetrics",properties);
        environment.getPropertySources().addLast(propertiesPropertySource);
    }
}
