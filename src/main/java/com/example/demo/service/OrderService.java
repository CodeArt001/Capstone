package com.example.demo.service;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CartItemRedisDTO;
import com.example.demo.dto.CartRedisDTO;
import com.example.demo.dto.OrderItemResponseDTO;
import com.example.demo.dto.OrderResponseDTO;
import com.example.demo.dto.PaymentResponseDTO;
import com.example.demo.dto.PaymentRequestDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.enums.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    
    
    private static final BigDecimal FIXED_SHIPPING_FEE = new BigDecimal("10.00");

    @Transactional
    public OrderResponseDTO checkout(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> userEmail.contains(u.getEmail()) || userEmail.contains(u.getId().toString()))
                        .findFirst()

                        .orElseThrow(() -> new RuntimeException("User not found: " + userEmail)));

        CartRedisDTO cart = cartService.getRawCart(userEmail);
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout with an empty cart");
        }

        // Validate stock & build OrderItems
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .shippingFee(FIXED_SHIPPING_FEE)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

     for (CartItemRedisDTO cartItem : cart.getItems()) {
    
    Product product = productRepository.findById(cartItem.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + cartItem.getProductId()));

    if (product.getStockQuantity() < cartItem.getQuantity()) {
        throw new RuntimeException("Insufficient stock for product: " + product.getName());
    }

    BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    subtotal = subtotal.add(lineTotal);

    OrderItem orderItem = OrderItem.builder()
            .order(order)
            .product(product) // Pass the actual managed Product entity
            .productName(product.getName())
            .productNameAtPurchase(product.getName())
            .productImageUrl(product.getImageUrl())
            .unitPrice(product.getPrice())
            .priceAtPurchase(product.getPrice())
            .quantity(cartItem.getQuantity())
            .totalPrice(lineTotal)
            .build();

    order.getItems().add(orderItem);
}

        BigDecimal grandTotal = subtotal.add(FIXED_SHIPPING_FEE);
        order.setSubtotal(subtotal);
        order.setGrandTotal(grandTotal);
        order.setTotalAmount(grandTotal);
        order.setPriceAtPurchase(grandTotal);

        Order savedOrder = orderRepository.save(order);

        
        cartService.clearCart(userEmail);

        return mapToResponseDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getUserOrders(String userEmail) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
            List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                            .map(item -> OrderItemResponseDTO.builder()
                                            .id(item.getId())
                                            .productId(item.getProduct().getId()) // <--- UPDATE THIS LINE
                                            .productName(item.getProductName())
                                            .productImageUrl(item.getProductImageUrl())
                                            .unitPrice(item.getUnitPrice())
                                            .quantity(item.getQuantity())
                                            .totalPrice(item.getTotalPrice())
                                            .build())
                            .collect(Collectors.toList());

            return OrderResponseDTO.builder()
                            .id(order.getId())
                            .userEmail(order.getUser().getEmail())
                            .status(order.getStatus())
                            .subtotal(order.getSubtotal())
                            .shippingFee(order.getShippingFee())
                            .grandTotal(order.getGrandTotal())
                            .items(itemDTOs)
                            .createdAt(order.getCreatedAt())
                            .build();
    }
@Transactional
public PaymentResponseDTO processPayment(PaymentRequestDTO request, String userEmail) {
    Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));

    if (!order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
        throw new RuntimeException("Unauthorized to pay for this order");
    }

    if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
        throw new RuntimeException("Order cannot be paid. Current status: " + order.getStatus());
    }

    order.setStatus(OrderStatus.PAID);
    Order updatedOrder = orderRepository.save(order);

    String transactionRef = "TXN-" + System.currentTimeMillis();

    return PaymentResponseDTO.builder()
            .OrderId(updatedOrder.getId())
            .status(updatedOrder.getStatus())
            .amountPaid(updatedOrder.getGrandTotal())
            .transactionalId(transactionRef)
            .message("Payment processed successfully")
            .paidAt(LocalDateTime.now())
            .build();
}


public OrderResponseDTO getOrderById(Long id, String username) {
    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

  
    if (!order.getUser().getEmail().equals(username)) {
        throw new RuntimeException("You do not have permission to view this order");
    }

 
    return mapToResponseDTO(order);
}
}