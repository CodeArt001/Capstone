package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import com.example.demo.dto.CheckoutRequestDTO;
import com.example.demo.dto.CartItemRedisDTO;
import com.example.demo.dto.CartRedisDTO;
import com.example.demo.dto.OrderItemResponseDTO;
import com.example.demo.dto.OrderResponseDTO;
import com.example.demo.dto.PaymentRequestDTO;
import com.example.demo.dto.PaymentResponseDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.PaymentTransaction;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentTransactionRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaystackService paystackService;
    private final String paystackCurrency;

    private static final BigDecimal FIXED_SHIPPING_FEE = new BigDecimal("10.00");

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CartService cartService,
            PaymentTransactionRepository paymentTransactionRepository,
            PaystackService paystackService,
            @Value("${paystack.currency:NGN}") String paystackCurrency) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paystackService = paystackService;
        this.paystackCurrency = paystackCurrency;
    }

    @Transactional
    public OrderResponseDTO checkout(String userEmail, CheckoutRequestDTO request) {
        String paymentReference = request.getReference().trim();
        if (paymentTransactionRepository.existsByProviderReference(paymentReference)) {
            throw new RuntimeException("Payment reference has already been used");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        CartRedisDTO cart = cartService.getRawCart(userEmail);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout with an empty cart");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<Product> products = new ArrayList<>();

        for (CartItemRedisDTO cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + cartItem.getProductId()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            products.add(product);
        }

        BigDecimal grandTotal = subtotal.add(FIXED_SHIPPING_FEE);
        long expectedAmount = grandTotal
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact();
        PaystackTransaction verifiedPayment = paystackService.verifyTransaction(paymentReference);
        if (!"success".equalsIgnoreCase(verifiedPayment.status())
                || !paymentReference.equals(verifiedPayment.reference())
                || verifiedPayment.amount() != expectedAmount
                || !paystackCurrency.equalsIgnoreCase(verifiedPayment.currency())) {
            throw new RuntimeException("Paystack payment does not match the cart total and currency");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PAID)
                .shippingFee(FIXED_SHIPPING_FEE)
                .build();

        for (int i = 0; i < cart.getItems().size(); i++) {
            CartItemRedisDTO cartItem = cart.getItems().get(i);
            Product product = products.get(i);
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
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

        order.setSubtotal(subtotal);
        order.setGrandTotal(grandTotal);
        order.setTotalAmount(grandTotal);
        order.setPriceAtPurchase(grandTotal);

        Order savedOrder = orderRepository.save(order);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setOrder(savedOrder);
        paymentTransaction.setProviderReference(paymentReference);
        paymentTransaction.setAmount(grandTotal);
        paymentTransaction.setStatus(PaymentStatus.SUCCESS);
        paymentTransactionRepository.save(paymentTransaction);

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

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id, String username) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (!order.getUser().getEmail().equalsIgnoreCase(username)) {
            throw new RuntimeException("You do not have permission to view this order");
        }

        return mapToResponseDTO(order);
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
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
}