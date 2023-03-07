package com.thoughtworks.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AddResponseHeaderFilterTest {

    AddResponseHeaderFilter addResponseHeaderFilter = new AddResponseHeaderFilter();

    @Before
    public void beforeSetUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddResponseHeaderFilter() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        addResponseHeaderFilter.doFilter(request, response, filterChain);

        verify(response).addHeader("Referrer-Policy", "strict-origin");
        verify(response).addHeader("Content-Security-Policy", "script-src 'unsafe-inline' 'self'");
        verify(response).addHeader("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains");
    }
}
