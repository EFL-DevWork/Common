package com.thoughtworks.serviceclients;

import com.thoughtworks.serviceclients.util.TracingUtil;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class HttpClient {
    private final OkHttpClient client;

    public HttpClient() {
        client = new OkHttpClient();
    }

    public int get(String baseUrl, Map<String, String> parameters) throws IOException {
        Request.Builder requestBuilder = new Request.Builder();
        TracingUtil.injectSpanCtx(requestBuilder);
        Request request = requestBuilder
                .url(baseUrl + getQuery(parameters))
                .get()
                .build();
        Response response = client.newCall(request).execute();
        return response.code();
    }


    public int post(String baseUrl, Map<String, String> reqBody) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new JSONObject(reqBody).toJSONString());
        Request.Builder requestBuilder = new Request.Builder();
        TracingUtil.injectSpanCtx(requestBuilder);
        Request request = requestBuilder
                .url(baseUrl)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.code();
    }

    private String getQuery(Map<String, String> parameters) {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        return result.toString();
    }
}
