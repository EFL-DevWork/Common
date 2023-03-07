package com.thoughtworks.filter;

import com.thoughtworks.metrics.Prometheus;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.servlet.*;
import java.io.IOException;

@Service
@Order(3)
public class PrometheusFilter implements Filter {
    @Autowired
    private Prometheus prometheus;

    private Counter paymentsCounter;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        paymentsCounter = prometheus.getPaymentsCounter();
        paymentsCounter.increment();
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
