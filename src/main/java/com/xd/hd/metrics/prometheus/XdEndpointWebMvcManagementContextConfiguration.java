package com.xd.hd.metrics.prometheus;
import io.micrometer.spring.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.*;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMappingCustomizer;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpointSecurityInterceptor;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoints;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Set;
@ManagementContextConfiguration
@EnableConfigurationProperties({ HealthMvcEndpointProperties.class,
        EndpointCorsProperties.class})
@ConditionalOnClass({PrometheusScrapeEndpoint.class})
public class XdEndpointWebMvcManagementContextConfiguration {

    private final MvcEndpoints mvcEndpoints;

    private final ManagementServerProperties managementServerProperties;

    private final EndpointCorsProperties corsProperties;

    private final List<EndpointHandlerMappingCustomizer> mappingCustomizers;

    public XdEndpointWebMvcManagementContextConfiguration(MvcEndpoints mvcEndpoints,
                                                            ManagementServerProperties managementServerProperties,
                                                            EndpointCorsProperties corsProperties,
                                                            ObjectProvider<List<EndpointHandlerMappingCustomizer>> mappingCustomizers, PrometheusScrapeEndpoint prometheusScrapeEndpoint) {
        this.mvcEndpoints = mvcEndpoints;
        this.managementServerProperties = managementServerProperties;
        this.corsProperties = corsProperties;
        List<EndpointHandlerMappingCustomizer> providedCustomizers = mappingCustomizers
                .getIfAvailable();
        this.mappingCustomizers = providedCustomizers == null
                ? Collections.<EndpointHandlerMappingCustomizer>emptyList()
                : providedCustomizers;
        prometheusScrapeEndpoint.setSensitive(false);
    }



    @Bean
    public XdEndpointHandlerMapping endpointHandlerMapping() {
        Set<MvcEndpoint> endpoints = mvcEndpoints.getEndpoints();
        CorsConfiguration corsConfiguration = getCorsConfiguration(this.corsProperties);
        XdEndpointHandlerMapping mapping = new XdEndpointHandlerMapping(endpoints,
                corsConfiguration);
        mapping.setPrefix(this.managementServerProperties.getContextPath());
        MvcEndpointSecurityInterceptor securityInterceptor = new MvcEndpointSecurityInterceptor(
                this.managementServerProperties.getSecurity().isEnabled(),
                this.managementServerProperties.getSecurity().getRoles());
        mapping.setSecurityInterceptor(securityInterceptor);
        for (EndpointHandlerMappingCustomizer customizer : this.mappingCustomizers) {
            customizer.customize(mapping);
        }
        return mapping;
    }

    private CorsConfiguration getCorsConfiguration(EndpointCorsProperties properties) {
        if (CollectionUtils.isEmpty(properties.getAllowedOrigins())) {
            return null;
        }
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.getAllowedOrigins());
        if (!CollectionUtils.isEmpty(properties.getAllowedHeaders())) {
            configuration.setAllowedHeaders(properties.getAllowedHeaders());
        }
        if (!CollectionUtils.isEmpty(properties.getAllowedMethods())) {
            configuration.setAllowedMethods(properties.getAllowedMethods());
        }
        if (!CollectionUtils.isEmpty(properties.getExposedHeaders())) {
            configuration.setExposedHeaders(properties.getExposedHeaders());
        }
        if (properties.getMaxAge() != null) {
            configuration.setMaxAge(properties.getMaxAge());
        }
        if (properties.getAllowCredentials() != null) {
            configuration.setAllowCredentials(properties.getAllowCredentials());
        }
        return configuration;
    }
}
