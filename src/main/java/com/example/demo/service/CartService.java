package com.example.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CartItemRequestDTO;
import com.example.demo.dto.CartItemResponseDTO;
import com.example.demo.dto.CartItemRedisDTO;
import com.example.demo.dto.CartRedisDTO;
import com.example.demo.dto.CartResponseDTO;
import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_TTL_DAYS = 7;

    private String getCartKey(String userEmail) {
        return CART_KEY_PREFIX + userEmail;
    }

    public CartResponseDTO getCart(String userEmail) {
        CartRedisDTO cart = fetchOrCreateCart(userEmail);
        return buildCartResponse(cart);
    }

    public CartResponseDTO addToCart(String userEmail, CartItemRequestDTO dto) {
        Product product = productRepository.findByIdAndActiveTrue(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found or inactive with id: " + dto.getProductId()));

        if (product.getStockQuantity() < dto.getQuantity()) {
            throw new RuntimeException("Requested quantity exceeds available stock (" + product.getStockQuantity() + ")");
        }

        CartRedisDTO cart = fetchOrCreateCart(userEmail);

        // Find existing item WITH MATCHING size and color
        Optional<CartItemRedisDTO> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId())
                        && Objects.equals(item.getSize(), dto.getSize())
                        && Objects.equals(item.getColor(), dto.getColor()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItemRedisDTO existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + dto.getQuantity();

            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Requested total quantity exceeds available stock");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setUnitPrice(product.getPrice());
        } else {
            CartItemRedisDTO newItem = CartItemRedisDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImageUrl(product.getImageUrl())
                    .unitPrice(product.getPrice())
                    .quantity(dto.getQuantity())
                    .size(dto.getSize())    // UPDATED: Set size
                    .color(dto.getColor())  // UPDATED: Set color
                    .build();
            cart.getItems().add(newItem);
        }

        saveCart(userEmail, cart);
        return buildCartResponse(cart);
    }

    public CartResponseDTO updateCartItemQuantity(String userEmail, Long productId, Integer quantity) {
        CartRedisDTO cart = fetchOrCreateCart(userEmail);

        if (quantity <= 0) {
            return removeCartItem(userEmail, productId);
        }

        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Product not found or inactive with id: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Requested quantity exceeds available stock");
        }

        boolean found = false;
        for (CartItemRedisDTO item : cart.getItems()) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                item.setUnitPrice(product.getPrice());
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException("Item not found in cart with product id: " + productId);
        }

        saveCart(userEmail, cart);
        return buildCartResponse(cart);
    }

    public CartResponseDTO removeCartItem(String userEmail, Long productId) {
        CartRedisDTO cart = fetchOrCreateCart(userEmail);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        saveCart(userEmail, cart);
        return buildCartResponse(cart);
    }

    public void clearCart(String userEmail) {
        redisTemplate.delete(getCartKey(userEmail));
    }

    public CartRedisDTO getRawCart(String userEmail) {
        return fetchOrCreateCart(userEmail);
    }

    private CartRedisDTO fetchOrCreateCart(String userEmail) {
        String key = getCartKey(userEmail);
        Object obj = redisTemplate.opsForValue().get(key);

        if (obj instanceof CartRedisDTO) {
            return (CartRedisDTO) obj;
        }

        return CartRedisDTO.builder()
                .userEmail(userEmail)
                .items(new ArrayList<>())
                .build();
    }

    private void saveCart(String userEmail, CartRedisDTO cart) {
        String key = getCartKey(userEmail);
        redisTemplate.opsForValue().set(key, cart, CART_TTL_DAYS, TimeUnit.DAYS);
    }

    private CartResponseDTO buildCartResponse(CartRedisDTO cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream().map(item -> {
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            return CartItemResponseDTO.builder()
                    .id(item.getProductId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .productImageUrl(item.getProductImageUrl())
                    .unitPrice(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .size(item.getSize())  
                    .color(item.getColor())  
                    .totalPrice(itemTotal)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal grandTotal = itemDTOs.stream()
                .map(CartItemResponseDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemDTOs.stream()
                .mapToInt(CartItemResponseDTO::getQuantity)
                .sum();

        return CartResponseDTO.builder()
                .items(itemDTOs)
                .grandTotal(grandTotal)
                .totalItems(totalItems)
                .build();
    }
}