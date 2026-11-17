package com.codekeys.securite;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Per-instance abuse protection. Only the socket address is trusted, never client-supplied forwarding headers. */
public class AntiSpamFilter extends OncePerRequestFilter {
    private static final Set<String> WRITES = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final int MAX_BUCKETS = 10000;
    private final Map<String, Window> windows = new HashMap<>();
    private final Clock clock;

    public AntiSpamFilter() { this(Clock.systemUTC()); }
    AntiSpamFilter(Clock clock) { this.clock = clock; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!WRITES.contains(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        String address = request.getRemoteAddr();
        long retryAfter = reserve(address + ":writes", 180, 60000);
        String path = request.getServletPath();
        if (retryAfter == 0 && path.equals("/authentification/connexion")) {
            retryAfter = reserve(address + ":login", 20, 60000);
        } else if (retryAfter == 0 && (path.equals("/adherents/mot-de-passe-oublier")
                || path.equals("/mot-de-passe-oublier") || path.equals("/adherents/reinitialiser-mot-de-passe"))) {
            retryAfter = reserve(address + ":password", 5, 900000);
        }
        if (retryAfter > 0) {
            response.setStatus(429);
            response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter));
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"statusCode\":429,\"message\":\"Trop de tentatives. Veuillez patienter avant de réessayer.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private synchronized long reserve(String key, int limit, long duration) {
        long now = clock.millis();
        Window window = windows.get(key);
        if (window == null || now >= window.expiresAt) {
            if (windows.size() >= MAX_BUCKETS) {
                windows.values().removeIf(value -> now >= value.expiresAt);
                if (windows.size() >= MAX_BUCKETS) return 60;
            }
            window = new Window(now + duration);
            windows.put(key, window);
        }
        if (window.count >= limit) return Math.max(1, (window.expiresAt - now + 999) / 1000);
        window.count++;
        return 0;
    }

    private static class Window {
        final long expiresAt;
        int count;
        Window(long expiresAt) { this.expiresAt = expiresAt; }
    }
}
