package com.thoughtworks.serviceclients.util;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TracingUtil {

    @Value("${service.name}")
    String serviceName;

    public static void injectSpanCtx(Request.Builder reqBuilder) {
        TextMapSetter<Request.Builder> setter = (carrier, key, value) -> carrier.header(key, value);

        GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), reqBuilder, setter);
    }

    public Span startSpan(String name, Context ctx) {
        Tracer tracer = GlobalOpenTelemetry.getTracer(serviceName);
        Span span = tracer.spanBuilder(name).setParent(ctx).startSpan();
        Context ctx1 = span.storeInContext(Context.current());
        ctx1.makeCurrent();
        return span;
    }
}
