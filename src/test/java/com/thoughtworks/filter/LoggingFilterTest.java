package com.thoughtworks.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.FilterChain;
import java.util.*;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoggingFilterTest {

    LoggingFilter loggingFilter = new LoggingFilter();

    @Before
    public void beforeSetUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLoggingFilter() throws Exception {
        FilterChain filterChain = mock(FilterChain.class);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockRequest.setMethod("POST");
        mockRequest.setServletPath("/bankinfo");
        mockRequest.setContentType("application/json");
        mockResponse.setStatus(201);

        Logger logger = (Logger) LoggerFactory.getLogger(LoggingFilter.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        List<ILoggingEvent> logsList = listAppender.list;

        loggingFilter.doFilter(mockRequest, mockResponse, filterChain);

        ILoggingEvent requestLog = logsList.get(0);
        ILoggingEvent responseLog = logsList.get(1);

        assertEquals("INFO", requestLog.getLevel().toString());
        assertEquals("POST /bankinfo", requestLog.getMessage());
        assertEquals("event_code=REQUEST_RECEIVED", requestLog.getArgumentArray()[0].toString());
        assertEquals("headers={\"Content-Type\":\"application/json\"}", requestLog.getArgumentArray()[1].toString());
        assertEquals("params={}", requestLog.getArgumentArray()[2].toString());

        assertEquals("INFO", responseLog.getLevel().toString());
        assertEquals("POST /bankinfo", responseLog.getMessage());
        assertEquals("event_code=RESPONSE_SENT", responseLog.getArgumentArray()[0].toString());
        assertEquals("status_code=201", responseLog.getArgumentArray()[1].toString());
    }
}
