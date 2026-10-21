package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data 
public class PaymentRequestDTO {

    @NotNull(message = "Order ID is required")
    private long orderId;

    private String paymentMethod;
}
