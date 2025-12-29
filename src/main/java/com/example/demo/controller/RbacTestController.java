package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-rbac")
@RequiredArgsConstructor
public class RbacTestController {

    @GetMapping("/marketing")
    public ResponseEntity<ApiResponse<String>> marketingTest() {
        return ResponseUtil.ok("Welcome Marketing Staff! You have access to this endpoint.", "Access Granted");
    }

    @GetMapping("/branch-manager")
    public ResponseEntity<ApiResponse<String>> managerTest() {
        return ResponseUtil.ok("Welcome Branch Manager! You have access to this endpoint.", "Access Granted");
    }

    @GetMapping("/back-office")
    public ResponseEntity<ApiResponse<String>> backOfficeTest() {
        return ResponseUtil.ok("Welcome Back Office Staff! You have access to this endpoint.", "Access Granted");
    }

    @GetMapping("/admin-only")
    public ResponseEntity<ApiResponse<String>> adminTest() {
        return ResponseUtil.ok("Welcome Admin! You have total access.", "Access Granted");
    }
}
