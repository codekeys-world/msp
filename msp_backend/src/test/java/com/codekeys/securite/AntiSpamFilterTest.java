package com.codekeys.securite;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AntiSpamFilterTest {
    @Test void blocksExcessivePasswordRequestsAndIgnoresSpoofedForwardingHeaders() throws Exception {
        var filter = new AntiSpamFilter();
        var passed = new AtomicInteger();
        for (int i = 0; i < 6; i++) {
            var request = request("POST", "/adherents/mot-de-passe-oublier", "192.0.2.1");
            request.addHeader("X-Forwarded-For", "192.0.2." + (i + 2));
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (req, res) -> passed.incrementAndGet());
            assertEquals(i < 5 ? 200 : 429, response.getStatus());
            if (i == 5) assertNotNull(response.getHeader("Retry-After"));
        }
        assertEquals(5, passed.get());
        var response = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/adherents/mot-de-passe-oublier", "192.0.2.2"), response, (req, res) -> {});
        assertEquals(200, response.getStatus());
    }

    @Test void permitsRetryAfterWindowAndLeavesReadRequestsUnrestricted() throws Exception {
        Clock clock = mock(Clock.class);
        when(clock.millis()).thenReturn(Instant.parse("2026-11-16T12:00:00Z").toEpochMilli());
        var filter = new AntiSpamFilter(clock);
        for (int i = 0; i < 21; i++) filter.doFilter(request("POST", "/authentification/connexion", "192.0.2.1"), new MockHttpServletResponse(), (req, res) -> {});
        when(clock.millis()).thenReturn(Instant.parse("2026-11-16T12:01:01Z").toEpochMilli());
        var response = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/authentification/connexion", "192.0.2.1"), response, (req, res) -> {});
        assertEquals(200, response.getStatus());
        for (int i = 0; i < 200; i++) {
            response = new MockHttpServletResponse();
            filter.doFilter(request("GET", "/adherents", "192.0.2.1"), response, (req, res) -> {});
            assertEquals(200, response.getStatus());
        }
    }

    private MockHttpServletRequest request(String method, String path, String ip) {
        var request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        request.setRemoteAddr(ip);
        return request;
    }
}
