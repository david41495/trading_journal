package com.davidserrano.tradejournal.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "app_users", uniqueConstraints =
        @UniqueConstraint(name = "uk_app_users_email", columnNames = "email"))
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AppUser() {
    }

    public AppUser(String displayName, String email, String passwordHash) {
        this.displayName = displayName.trim();
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
    }

    @PrePersist
    void beforeInsert() {
        createdAt = LocalDateTime.now();
        displayName = displayName.trim();
        email = email.trim().toLowerCase();
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
