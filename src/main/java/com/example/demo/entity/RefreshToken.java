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

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
@JoinColumn(name = "user_id", nullable = false)
    private User user;
@Column(nullable = false, unique = true)
private String token;
 @Column(nullable = false)   
 private LocalDateTime expiresAt;
@Column(nullable = false)
private boolean revoked = false;

public RefreshToken() {
    
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

public String getToken() {
    return token;
}

public void setToken(String token) {
    this.token = token;
}

public LocalDateTime getExpiresAt() {
    return expiresAt;
}

public void setExpireAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
}

public boolean isRevoked() {
    return revoked;
}

public void setRevoked(boolean revoked) {
    this.revoked = revoked;
}

    
}
