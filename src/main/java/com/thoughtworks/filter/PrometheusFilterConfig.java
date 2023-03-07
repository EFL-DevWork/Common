package com.thoughtworks.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrometheusFilterConfig {
    @Autowired
    PrometheusFilter prometheusFilter;

    @Bean
    public FilterRegistrationBean<PrometheusFilter> filterRegistrationBean() {
        FilterRegistrationBean<PrometheusFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(prometheusFilter);
        registrationBean.addUrlPatterns("/payments");
        return registrationBean;
    }
}
