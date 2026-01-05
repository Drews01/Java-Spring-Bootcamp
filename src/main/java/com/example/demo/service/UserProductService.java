package com.example.demo.service;

import com.example.demo.dto.UserProductDTO;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProduct;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserProductRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProductService {

  private final UserProductRepository userProductRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  @Transactional
  public UserProductDTO createUserProduct(UserProductDTO dto) {
    User user =
        userRepository
            .findById(dto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

    Product product =
        productRepository
            .findById(dto.getProductId())
            .orElseThrow(
                () -> new RuntimeException("Product not found with id: " + dto.getProductId()));

    // Check if already exists
    userProductRepository
        .findByUser_IdAndProduct_Id(dto.getUserId(), dto.getProductId())
        .ifPresent(
            up -> {
              throw new RuntimeException(
                  "UserProduct already exists for user "
                      + dto.getUserId()
                      + " and product "
                      + dto.getProductId());
            });

    UserProduct userProduct =
        UserProduct.builder()
            .user(user)
            .product(product)
            .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
            .build();

    UserProduct saved = userProductRepository.save(userProduct);
    return convertToDTO(saved);
  }

  @Transactional(readOnly = true)
  public UserProductDTO getUserProduct(Long userProductId) {
    UserProduct userProduct =
        userProductRepository
            .findById(userProductId)
            .orElseThrow(
                () -> new RuntimeException("UserProduct not found with id: " + userProductId));
    return convertToDTO(userProduct);
  }

  @Transactional(readOnly = true)
  public List<UserProductDTO> getUserProductsByUserId(Long userId) {
    return userProductRepository.findByUser_Id(userId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<UserProductDTO> getActiveUserProductsByUserId(Long userId) {
    return userProductRepository.findByUser_IdAndStatus(userId, "ACTIVE").stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<UserProductDTO> getAllUserProducts() {
    return userProductRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public UserProductDTO updateUserProduct(Long userProductId, UserProductDTO dto) {
    UserProduct userProduct =
        userProductRepository
            .findById(userProductId)
            .orElseThrow(
                () -> new RuntimeException("UserProduct not found with id: " + userProductId));

    userProduct.setStatus(dto.getStatus());

    UserProduct updated = userProductRepository.save(userProduct);
    return convertToDTO(updated);
  }

  @Transactional
  public void deleteUserProduct(Long userProductId) {
    userProductRepository.deleteById(userProductId);
  }

  private UserProductDTO convertToDTO(UserProduct userProduct) {
    return UserProductDTO.builder()
        .userProductId(userProduct.getUserProductId())
        .userId(userProduct.getUser().getId())
        .productId(userProduct.getProduct().getId())
        .status(userProduct.getStatus())
        .createdAt(userProduct.getCreatedAt())
        .build();
  }
}
