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
@RequestMapping("/api/branch-manager")
@RequiredArgsConstructor
public class BranchManagerController {

  @GetMapping("/dashboard")
  @PreAuthorize("@accessControl.hasMenu('MANAGER_MODULE')")
  public ResponseEntity<ApiResponse<String>> getDashboard() {
    return ResponseUtil.ok(
        "Welcome to the Branch Manager Dashboard.", "Branch Manager access verified");
  }

  @GetMapping("/reports")
  @PreAuthorize("@accessControl.hasMenu('MANAGER_MODULE')")
  public ResponseEntity<ApiResponse<String>> getReports() {
    return ResponseUtil.ok(
        "Branch Manager reports data would go here.", "Branch Manager access verified");
  }
}
