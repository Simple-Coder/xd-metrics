package com.xd.hd.metrics.prometheus;

import io.micrometer.spring.export.prometheus.PrometheusScrapeMvcEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collection;

public class XdEndpointHandlerMapping extends EndpointHandlerMapping {

    public XdEndpointHandlerMapping(Collection<? extends MvcEndpoint> endpoints) {
        super(endpoints);
    }

    public XdEndpointHandlerMapping(Collection<? extends MvcEndpoint> endpoints, CorsConfiguration corsConfiguration) {
        super(endpoints, corsConfiguration);
    }

    @Override
    protected void detectHandlerMethods(Object handler) {
        String prefix = getPrefix();
        if (handler instanceof PrometheusScrapeMvcEndpoint) {
            setPrefix("/actuator");
        }
        super.detectHandlerMethods(handler);
        setPrefix(prefix);
    }
}
