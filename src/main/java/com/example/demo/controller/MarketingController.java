package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketing")
@RequiredArgsConstructor
public class MarketingController {

    @GetMapping("/dashboard")
    @PreAuthorize("@accessControl.hasMenu('MARKETING_MODULE')")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        return ResponseUtil.ok("Welcome to the Marketing Dashboard.", "Marketing access verified");
    }

    @GetMapping("/stats")
    @PreAuthorize("@accessControl.hasMenu('MARKETING_MODULE')")
    public ResponseEntity<ApiResponse<String>> getStats() {
        return ResponseUtil.ok("Marketing stats data would go here.", "Marketing access verified");
    }
}
