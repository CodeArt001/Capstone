package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.WishListItem;
@Repository
public interface WishListItemRepository extends JpaRepository<WishListItem, Long> {
    List<WishListItem> findByUserId(Long userId);
    Optional<WishListItem> findByUserIdAndProductId(Long userId, Long productId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}