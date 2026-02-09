package com.example.demo.dto;

import com.example.demo.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

  private Long id;
  private String code;
  private String name;
  private Double minAmount;
  private Double maxAmount;
  private Integer minTenureMonths;
  private Integer maxTenureMonths;
  private Double interestRate;
  private Double creditLimit;
  private Integer tierOrder;
  private Double upgradeThreshold;
  private Boolean isActive;

  public static ProductDTO fromEntity(Product product) {
    if (product == null) {
      return null;
    }
    return ProductDTO.builder()
        .id(product.getId())
        .code(product.getCode())
        .name(product.getName())
        .minAmount(product.getMinAmount())
        .maxAmount(product.getMaxAmount())
        .minTenureMonths(product.getMinTenureMonths())
        .maxTenureMonths(product.getMaxTenureMonths())
        .interestRate(product.getInterestRate())
        .creditLimit(product.getCreditLimit())
        .tierOrder(product.getTierOrder())
        .upgradeThreshold(product.getUpgradeThreshold())
        .isActive(product.getIsActive())
        .build();
  }
}
