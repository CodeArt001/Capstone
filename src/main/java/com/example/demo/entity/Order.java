package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.enums.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(nullable = false)
        private BigDecimal subtotal;
    @Column(nullable = false)
    private BigDecimal shippingFee;
    
    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
        
public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public User getUser() {
    return user;
}

public void setUser(User user) {
    this.user = user;
}

public OrderStatus getStatus() {
    return status;
}

public void setStatus(OrderStatus status) {
    this.status = status;
}

public BigDecimal getSubtotal() {
    return subtotal;
}

public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
}

public BigDecimal getShippingFee() {
    return shippingFee;
}

public void setShippingFee(BigDecimal shippingFee) {
    this.shippingFee = shippingFee;
}

public BigDecimal getTotalAmount() {
    return totalAmount;
}

public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}

public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
}

public List<OrderItem> getOrderItems() {
    return orderItems;
}

public void setOrderItems(List<OrderItem> orderItems) {
    this.orderItems = orderItems;
}


}
