package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.CartItemRedisDTO;
import com.example.demo.dto.CartRedisDTO;
import com.example.demo.dto.CheckoutRequestDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.PaymentTransaction;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentTransactionRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartService cartService;
    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;
    @Mock
    private PaystackService paystackService;

    private OrderService orderService;
    private CheckoutRequestDTO request;
    private CartRedisDTO cart;
    private Product product;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                productRepository,
                userRepository,
                cartService,
                paymentTransactionRepository,
                paystackService,
                "NGN");
        request = new CheckoutRequestDTO();
        request.setReference("paystack-ref-123");

        product = Product.builder()
                .id(7L)
                .name("Test product")
                .price(new BigDecimal("25.50"))
                .stockQuantity(4)
                .build();
        cart = CartRedisDTO.builder()
                .items(List.of(CartItemRedisDTO.builder()
                        .productId(7L)
                        .quantity(2)
                        .build()))
                .build();

    }

    @Test
    void createsPaidOrderOnlyAfterSuccessfulMatchingPayment() {
        givenCartAndUser();
        when(paystackService.verifyTransaction(request.getReference()))
                .thenReturn(new PaystackTransaction("paystack-ref-123", "success", 6100, "NGN"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.checkout("buyer@example.com", request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.PAID, savedOrder.getStatus());
        assertEquals(new BigDecimal("61.00"), savedOrder.getGrandTotal());

        ArgumentCaptor<PaymentTransaction> paymentCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(paymentTransactionRepository).save(paymentCaptor.capture());
        assertEquals("paystack-ref-123", paymentCaptor.getValue().getProviderReference());
        assertEquals(PaymentStatus.SUCCESS, paymentCaptor.getValue().getStatus());
        assertEquals(new BigDecimal("61.00"), paymentCaptor.getValue().getAmount());
        verify(cartService).clearCart("buyer@example.com");
    }

    @Test
    void doesNotCreateOrderWhenPaystackRejectsPayment() {
        givenCartAndUser();
        doThrow(new IllegalArgumentException("Paystack did not confirm a successful payment"))
                .when(paystackService).verifyTransaction(request.getReference());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout("buyer@example.com", request));

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentTransactionRepository, never()).save(any(PaymentTransaction.class));
        verify(cartService, never()).clearCart("buyer@example.com");
    }

    @Test
    void doesNotCreateOrderWhenVerifiedAmountDoesNotMatchCartTotal() {
        givenCartAndUser();
        when(paystackService.verifyTransaction(request.getReference()))
                .thenReturn(new PaystackTransaction("paystack-ref-123", "success", 6000, "NGN"));

        assertThrows(RuntimeException.class,
                () -> orderService.checkout("buyer@example.com", request));

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentTransactionRepository, never()).save(any(PaymentTransaction.class));
    }

    @Test
    void doesNotCreateOrderWhenVerifiedCurrencyDoesNotMatch() {
        givenCartAndUser();
        when(paystackService.verifyTransaction(request.getReference()))
                .thenReturn(new PaystackTransaction("paystack-ref-123", "success", 6100, "USD"));

        assertThrows(RuntimeException.class,
                () -> orderService.checkout("buyer@example.com", request));

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentTransactionRepository, never()).save(any(PaymentTransaction.class));
    }

    @Test
    void rejectsPreviouslyUsedPaymentReferenceBeforeVerification() {
        when(paymentTransactionRepository.existsByProviderReference(request.getReference()))
                .thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> orderService.checkout("buyer@example.com", request));

        verify(paystackService, never()).verifyTransaction(any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    private void givenCartAndUser() {
        when(paymentTransactionRepository.existsByProviderReference(request.getReference()))
                .thenReturn(false);
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(user("buyer@example.com")));
        when(cartService.getRawCart("buyer@example.com")).thenReturn(cart);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        return user;
    }
}
