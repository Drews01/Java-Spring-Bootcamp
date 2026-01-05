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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

  @GetMapping("/dashboard")
  @PreAuthorize("@accessControl.hasMenu('ADMIN_MODULE')")
  public ResponseEntity<ApiResponse<String>> getDashboard() {
    return ResponseUtil.ok(
        "Welcome to the System Administration Dashboard.", "Admin access verified");
  }

  @GetMapping("/system-logs")
  @PreAuthorize("@accessControl.hasMenu('ADMIN_MODULE')")
  public ResponseEntity<ApiResponse<String>> getSystemLogs() {
    return ResponseUtil.ok("System logs would be visible here.", "Admin access verified");
  }
}
