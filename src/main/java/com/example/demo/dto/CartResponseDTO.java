package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResponseDTO {
    private List<CartItemResponseDTO> items;
    private BigDecimal grandTotal;
    private Integer totalItems;
}
