package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/**
 * Custom CSRF token request handler for Single Page Applications (SPAs).
 * Combines deferred token loading with BREACH protection.
 * 
 * <p>
 * For SPAs, the CSRF token needs to be:
 * 1. Loaded lazily to avoid generating tokens for unauthenticated requests
 * 2. Protected against BREACH attacks using XOR encoding
 * 3. Accessible to JavaScript for inclusion in AJAX requests
 */
public final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        // Always use XorCsrfTokenRequestAttributeHandler for BREACH protection
        this.delegate.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        // If the request contains a header, use the XOR handler to decode it
        // Otherwise, fall back to the default behavior (for form submissions)
        if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
            return super.resolveCsrfTokenValue(request, csrfToken);
        }
        // Delegate to XOR handler for proper BREACH protection decoding
        return this.delegate.resolveCsrfTokenValue(request, csrfToken);
    }
}
