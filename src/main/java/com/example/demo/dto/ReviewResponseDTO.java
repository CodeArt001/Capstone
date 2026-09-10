package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder 
public class ReviewResponseDTO {
    private Long id;
    private String userEmail;
    private Long productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
