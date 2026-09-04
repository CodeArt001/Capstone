package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.enums.OrderStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder 
public class PaymentResponseDTO {
    private Long OrderId;
    private OrderStatus status;
    private BigDecimal amountPaid;
    private String transactionalId;
    private String message;
    private LocalDateTime paidAt;
}
