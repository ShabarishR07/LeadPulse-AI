package com.marketing.leadscore.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_users")
public class DashboardUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "account_role", nullable = false, length = 20)
    private String role;

    @Column(nullable = false)
    private boolean approved;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected DashboardUser() {
    }

    public DashboardUser(String username, String passwordHash, String role, boolean approved,
                         LocalDateTime createdAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.approved = approved;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public boolean isApproved() {
        return approved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void approve() {
        approved = true;
    }

    public void configureAdministrator(String passwordHash) {
        role = "ADMIN";
        approved = true;
        this.passwordHash = passwordHash;
    }
}
