package com.marketing.leadscore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.boot.ApplicationRunner;
import com.marketing.leadscore.service.DashboardUserService;

import java.util.Arrays;

@Configuration
@Profile("production")
public class ProductionSecurityConfig {

    @Bean
    public SecurityFilterChain productionSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/tracking/**",
                        "/api/integrations/**",
                        "/api/analytics/tracking",
                        "/api/organizations/**",
                        "/api/privacy/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/favicon.ico")
                        .permitAll()
                        .requestMatchers("/admin/accounts/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/api/tracking/**",
                                "/api/integrations/**",
                                "/api/analytics/tracking",
                                "/api/organizations/**",
                                "/api/privacy/**").permitAll()
                        .anyRequest().authenticated())
                .cors(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", false)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .sessionManagement(session -> session.sessionFixation().migrateSession());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource productionCorsConfigurationSource(
            @Value("${leadpulse.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of(
                "Content-Type", "X-LeadPulse-Api-Key", "X-LeadPulse-Timestamp",
                "X-LeadPulse-Signature", "X-LeadPulse-Idempotency-Key"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder productionPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ApplicationRunner bootstrapAdministrator(
            DashboardUserService userService,
            @Value("${leadpulse.admin.username}") String username,
            @Value("${leadpulse.admin.password-bcrypt}") String passwordHash) {
        return args -> userService.ensureAdministrator(username, passwordHash);
    }
}
