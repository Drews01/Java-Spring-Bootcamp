package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.dto.UserProductDTO;
import com.example.demo.service.UserProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-products")
@RequiredArgsConstructor
public class UserProductController {

    private final UserProductService userProductService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserProductDTO>> createUserProduct(@RequestBody UserProductDTO dto) {
        UserProductDTO created = userProductService.createUserProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "User product created successfully"));
    }

    @GetMapping("/{userProductId}")
    public ResponseEntity<ApiResponse<UserProductDTO>> getUserProduct(@PathVariable Long userProductId) {
        UserProductDTO userProduct = userProductService.getUserProduct(userProductId);
        return ResponseEntity.ok(ApiResponse.success(userProduct, "User product retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UserProductDTO>>> getUserProductsByUserId(@PathVariable Long userId) {
        List<UserProductDTO> userProducts = userProductService.getUserProductsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(userProducts, "User products retrieved successfully"));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ApiResponse<List<UserProductDTO>>> getActiveUserProductsByUserId(@PathVariable Long userId) {
        List<UserProductDTO> userProducts = userProductService.getActiveUserProductsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(userProducts, "Active user products retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProductDTO>>> getAllUserProducts() {
        List<UserProductDTO> userProducts = userProductService.getAllUserProducts();
        return ResponseEntity.ok(ApiResponse.success(userProducts, "User products retrieved successfully"));
    }

    @PutMapping("/{userProductId}")
    public ResponseEntity<ApiResponse<UserProductDTO>> updateUserProduct(
            @PathVariable Long userProductId,
            @RequestBody UserProductDTO dto) {
        UserProductDTO updated = userProductService.updateUserProduct(userProductId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "User product updated successfully"));
    }

    @DeleteMapping("/{userProductId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserProduct(@PathVariable Long userProductId) {
        userProductService.deleteUserProduct(userProductId);
        return ResponseEntity.ok(ApiResponse.success(null, "User product deleted successfully"));
    }
}
