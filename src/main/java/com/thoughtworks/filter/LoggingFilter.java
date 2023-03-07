package com.thoughtworks.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.errorcodes.EventCodes;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@Order(2)
public class LoggingFilter implements Filter {

    @Value("${service.name}")
    String serviceName;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);

        Tracer tracer = GlobalOpenTelemetry.getTracer(serviceName);
        String spanName = String.format("%s %s", httpServletRequest.getMethod(), httpServletRequest.getServletPath());
        Span span = tracer.spanBuilder(spanName).startSpan();
        Context ctx = span.storeInContext(Context.current());
        ctx.makeCurrent();

        MDC.put("trace_id", span.getSpanContext().getTraceId());
        MDC.put("span_id", span.getSpanContext().getSpanId());

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            logRequest(httpServletRequest, requestWrapper);
            logResponse(httpServletRequest, httpServletResponse, responseWrapper);
            responseWrapper.copyBodyToResponse();
            MDC.clear();
            span.end();
        }
    }

    private void logResponse(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse,
                             ContentCachingResponseWrapper responseWrapper) {
        String responseBody = IOUtils.toString(responseWrapper.getContentAsByteArray(), UTF_8.name());
        log.info(String.format("%s %s", httpServletRequest.getMethod(), httpServletRequest.getServletPath()),
                kv("event_code", EventCodes.RESPONSE_SENT),
                kv("status_code", httpServletResponse.getStatus()),
                kv("body", getContent(responseBody)));
    }

    private void logRequest(HttpServletRequest httpServletRequest,
                            ContentCachingRequestWrapper requestWrapper)
            throws IOException {
        String requestBody = IOUtils.toString(requestWrapper.getContentAsByteArray(), UTF_8.name());
        log.info(String.format("%s %s", httpServletRequest.getMethod(), httpServletRequest.getServletPath()),
                kv("event_code", EventCodes.REQUEST_RECEIVED),
                kv("headers", mapToString(getHeaderMap(httpServletRequest))),
                kv("params", mapToString(httpServletRequest.getParameterMap())),
                kv("body", getContent(requestBody))
        );
    }

    private String mapToString(Map map) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(map);
    }

    private Map<String, String> getHeaderMap(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> en = request.getHeaderNames();
        while (en.hasMoreElements()) {
            String headerName = en.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }

    private Object getContent(String body) {
        if (body.length() > 1000) {
            return "payload is too large to log";
        }

        ObjectMapper objectMapper = new ObjectMapper();

        Object content;
        try {
            content = objectMapper.readValue(body, Object.class);
        } catch (Exception ex) {
            content = body;
        }
        return content;
    }
}
