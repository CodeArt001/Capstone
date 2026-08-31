package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CartItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
}
