package com.example.demo.entity;




import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "reviews", uniqueConstraints  = @UniqueConstraint(columnNames = {"user_id", "product_id"} ))
public class Review {
  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

@ManyToOne
@JoinColumn(name = "user_id", nullable = false)
private User user;
  
@ManyToOne
@JoinColumn(name = "product_id", nullable = false)
private Product product;
   
@Column(nullable = false)
private Integer rating;

@Column(columnDefinition = "TEXT")
private String comment;

@Column(nullable = false)
private LocalDateTime createdAt = LocalDateTime.now();

public Review() {

}

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

public Product getProduct() {
    return product;
}

public void setProduct(Product product) {
    this.product = product;
}

public Integer getrating() {
    return rating;
}

public void setInteger(Integer rating) {
    this.rating = rating;
}

public String getComment() {
    return comment;
}

public void setComment(String comment) {
    this.comment = comment;
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}

public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
}


}
