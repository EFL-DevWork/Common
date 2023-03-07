package com.thoughtworks.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
@Order(1)
public class AddResponseHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        httpServletResponse.addHeader("Referrer-Policy", "strict-origin");
        httpServletResponse.addHeader("Content-Security-Policy", "script-src 'unsafe-inline' 'self'");
        httpServletResponse.addHeader("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains");
        chain.doFilter(request, httpServletResponse);
    }

}