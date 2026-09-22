package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CartItemRequestDTO;
import com.example.demo.dto.CartResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Helper method to safely extract user email from Authentication principal
    private String getEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getEmail();
        }
        return authentication.getName();
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(getEmail(authentication)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequestDTO dto) {
        return ResponseEntity.ok(cartService.addToCart(getEmail(authentication), dto));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> updateQuantity(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(getEmail(authentication), productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            Authentication authentication,
            @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeCartItem(getEmail(authentication), productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(getEmail(authentication));
        return ResponseEntity.noContent().build();
    }
}