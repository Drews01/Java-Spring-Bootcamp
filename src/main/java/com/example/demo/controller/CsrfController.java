package com.example.demo.controller;

import com.example.demo.dto.CsrfTokenDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for CSRF token endpoint.
 * Provides CSRF token for SPA frontend to include in state-changing requests.
 */
@RestController
@RequestMapping("/api")
public class CsrfController {

    /**
     * Get CSRF token for SPA frontend.
     * The token should be included in X-XSRF-TOKEN header for POST/PUT/DELETE
     * requests.
     *
     * @param csrfToken injected by Spring Security
     * @return CSRF token and header name
     */
    @GetMapping("/csrf-token")
    public ResponseEntity<CsrfTokenDTO> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.ok(
                new CsrfTokenDTO(csrfToken.getToken(), csrfToken.getHeaderName()));
    }
}
