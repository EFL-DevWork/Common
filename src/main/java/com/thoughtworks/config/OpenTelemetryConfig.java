package com.thoughtworks.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class OpenTelemetryConfig {


    @Bean
    public OpenTelemetry openTelemetry() {
        return GlobalOpenTelemetry.get();
    }

    @Value("${jaeger.endpoint}")
    String endpoint;

    @Value("${service.name}")
    String serviceName;

    @Value("${jaeger.enabled}")
    boolean jaegerEnabled;

    @PostConstruct
    public void setUpOpenTelemetry() {
        Resource serviceNameResource =
                Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName));

        JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
                .setEndpoint(endpoint)
                .build();

        SdkTracerProviderBuilder tracerProviderBuilder = SdkTracerProvider.builder()
                .setResource(Resource.getDefault().merge(serviceNameResource));

        SdkTracerProvider tracerProvider = !jaegerEnabled ? tracerProviderBuilder.build() : tracerProviderBuilder
                .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
                .build();

        OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();
    }
}



