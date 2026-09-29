package com.marketing.leadscore.service;

import com.marketing.leadscore.entity.DashboardUser;
import com.marketing.leadscore.repository.DashboardUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Profile("production")
@Transactional
public class DashboardUserService implements UserDetailsService {

    private final DashboardUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DashboardUserService(DashboardUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DashboardUser account = repository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .roles(account.getRole())
                .disabled(!account.isApproved())
                .build();
    }

    public void register(String username, String rawPassword, String confirmation, String administratorUsername) {
        String normalizedUsername = normalizeUsername(username);
        if (!normalizedUsername.matches("[a-z0-9._-]{3,50}")) {
            throw new IllegalArgumentException("Use 3–50 letters, numbers, dots, underscores, or hyphens.");
        }
        if (normalizedUsername.equals(normalizeUsername(administratorUsername))) {
            throw new IllegalArgumentException("That username is reserved.");
        }
        if (rawPassword == null || rawPassword.length() < 12 || rawPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be at least 12 characters and no more than 72 UTF-8 bytes.");
        }
        if (!rawPassword.equals(confirmation)) {
            throw new IllegalArgumentException("The password confirmation does not match.");
        }
        if (repository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("That username is already registered.");
        }
        repository.save(new DashboardUser(
                normalizedUsername,
                passwordEncoder.encode(rawPassword),
                "USER",
                false,
                LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public List<DashboardUser> getPendingUsers() {
        return repository.findAllByRoleOrderByCreatedAtAsc("USER").stream()
                .filter(user -> !user.isApproved())
                .toList();
    }

    public void approve(Long userId) {
        DashboardUser user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (!"USER".equals(user.getRole())) {
            throw new IllegalArgumentException("Only standard user accounts can be approved here.");
        }
        user.approve();
    }

    public void ensureAdministrator(String username, String passwordHash) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername.isBlank() || passwordHash == null
                || !passwordHash.matches("^\\$2[aby]\\$.{56}$")) {
            throw new IllegalStateException(
                    "Production requires LEADPULSE_ADMIN_USERNAME and a BCrypt LEADPULSE_ADMIN_PASSWORD_BCRYPT.");
        }
        DashboardUser administrator = repository.findByUsername(normalizedUsername)
                .orElseGet(() -> repository.save(new DashboardUser(
                        normalizedUsername, passwordHash, "ADMIN", true, LocalDateTime.now())));
        administrator.configureAdministrator(passwordHash);
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
