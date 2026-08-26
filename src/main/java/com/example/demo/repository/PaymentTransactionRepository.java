package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.PaymentTransaction;
@Repository
public interface PaymentTransactionRepository extends JpaRepository <PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByOrderId(Long orderId);
}
