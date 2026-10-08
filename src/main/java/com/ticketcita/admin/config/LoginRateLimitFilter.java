package com.ticketcita.admin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Freno contra adivinar la clave: maximo 8 intentos de login por IP cada 10 minutos. */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_INTENTOS = 8;
    private static final long VENTANA_MS = 10 * 60 * 1000L;

    private final Map<String, Deque<Long>> intentos = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod()) && "/login".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long ahora = System.currentTimeMillis();
        if (intentos.size() > 10_000) {
            intentos.clear();
        }
        Deque<Long> cola = intentos.computeIfAbsent(request.getRemoteAddr(), ip -> new ArrayDeque<>());
        synchronized (cola) {
            while (!cola.isEmpty() && ahora - cola.peekFirst() > VENTANA_MS) {
                cola.pollFirst();
            }
            if (cola.size() >= MAX_INTENTOS) {
                response.sendRedirect("/login?limite");
                return;
            }
            cola.addLast(ahora);
        }
        chain.doFilter(request, response);
    }
}
