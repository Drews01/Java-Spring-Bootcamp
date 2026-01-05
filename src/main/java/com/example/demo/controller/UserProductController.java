package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.UserProductDTO;
import com.example.demo.service.UserProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-products")
@RequiredArgsConstructor
public class UserProductController {

  private final UserProductService userProductService;

  @PostMapping
  public ResponseEntity<ApiResponse<UserProductDTO>> createUserProduct(
      @RequestBody UserProductDTO dto) {
    UserProductDTO created = userProductService.createUserProduct(dto);
    return ResponseUtil.created(created, "User product created successfully");
  }

  @GetMapping("/{userProductId}")
  public ResponseEntity<ApiResponse<UserProductDTO>> getUserProduct(
      @PathVariable Long userProductId) {
    UserProductDTO userProduct = userProductService.getUserProduct(userProductId);
    return ResponseUtil.ok(userProduct, "User product retrieved successfully");
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponse<List<UserProductDTO>>> getUserProductsByUserId(
      @PathVariable Long userId) {
    List<UserProductDTO> userProducts = userProductService.getUserProductsByUserId(userId);
    return ResponseUtil.ok(userProducts, "User products retrieved successfully");
  }

  @GetMapping("/user/{userId}/active")
  public ResponseEntity<ApiResponse<List<UserProductDTO>>> getActiveUserProductsByUserId(
      @PathVariable Long userId) {
    List<UserProductDTO> userProducts = userProductService.getActiveUserProductsByUserId(userId);
    return ResponseUtil.ok(userProducts, "Active user products retrieved successfully");
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<UserProductDTO>>> getAllUserProducts() {
    List<UserProductDTO> userProducts = userProductService.getAllUserProducts();
    return ResponseUtil.ok(userProducts, "User products retrieved successfully");
  }

  @PutMapping("/{userProductId}")
  public ResponseEntity<ApiResponse<UserProductDTO>> updateUserProduct(
      @PathVariable Long userProductId, @RequestBody UserProductDTO dto) {
    UserProductDTO updated = userProductService.updateUserProduct(userProductId, dto);
    return ResponseUtil.ok(updated, "User product updated successfully");
  }

  @DeleteMapping("/{userProductId}")
  public ResponseEntity<ApiResponse<Void>> deleteUserProduct(@PathVariable Long userProductId) {
    userProductService.deleteUserProduct(userProductId);
    return ResponseUtil.okMessage("User product deleted successfully");
  }
}
