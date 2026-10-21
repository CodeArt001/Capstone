package com.example.demo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRedisDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userEmail;
    @Builder.Default
    private List<CartItemRedisDTO> items = new ArrayList<>();
    private BigDecimal grandTotal;
    private Integer totalItems;
}
