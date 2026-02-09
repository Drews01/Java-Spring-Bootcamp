package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.UserProductDTO;
import com.example.demo.dto.UserTierLimitDTO;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-products")
@RequiredArgsConstructor
public class UserProductController {

  private final UserProductService userProductService;
  private final UserRepository userRepository;

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

  /**
   * Get the current authenticated user's product tier and credit limit information.
   *
   * @return UserTierLimitDTO with tier info, limits, and upgrade progress
   */
  @GetMapping("/my-tier")
  public ResponseEntity<ApiResponse<UserTierLimitDTO>> getMyTierAndLimits() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

    UserTierLimitDTO tierInfo = userProductService.getCurrentUserTierAndLimits(user.getId());

    if (tierInfo == null) {
      return ResponseUtil.ok(null, "No active tier found. User needs to subscribe to a product.");
    }

    return ResponseUtil.ok(tierInfo, "User tier and limits retrieved successfully");
  }
}
