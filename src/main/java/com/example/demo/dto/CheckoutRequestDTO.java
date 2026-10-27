package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequestDTO {

    @NotBlank(message = "Paystack reference is required")
    @Size(max = 255, message = "Payment reference is too long")
    private String reference;
}
