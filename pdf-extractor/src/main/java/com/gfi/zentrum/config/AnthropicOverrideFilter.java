package com.gfi.zentrum.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads per-request Anthropic overrides from HTTP headers and stores them
 * in {@link AnthropicOverrideHolder} for the duration of the request.
 */
@Component
public class AnthropicOverrideFilter extends OncePerRequestFilter {

    static final String HEADER_API_KEY = "X-Anthropic-Api-Key";
    static final String HEADER_BASE_URL = "X-Anthropic-Base-Url";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            AnthropicOverrideHolder.set(
                    request.getHeader(HEADER_API_KEY),
                    request.getHeader(HEADER_BASE_URL)
            );
            filterChain.doFilter(request, response);
        } finally {
            AnthropicOverrideHolder.clear();
        }
    }
}
