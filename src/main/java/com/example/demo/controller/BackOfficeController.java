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
@RequestMapping("/api/back-office")
@RequiredArgsConstructor
public class BackOfficeController {

  @GetMapping("/dashboard")
  @PreAuthorize("@accessControl.hasMenu('BACKOFFICE_MODULE')")
  public ResponseEntity<ApiResponse<String>> getDashboard() {
    return ResponseUtil.ok("Welcome to the Back Office Dashboard.", "Back Office access verified");
  }

  @GetMapping("/disbursements")
  @PreAuthorize("@accessControl.hasMenu('BACKOFFICE_MODULE')")
  public ResponseEntity<ApiResponse<String>> getDisbursements() {
    return ResponseUtil.ok(
        "Back Office disbursement tracking would go here.", "Back Office access verified");
  }
}
