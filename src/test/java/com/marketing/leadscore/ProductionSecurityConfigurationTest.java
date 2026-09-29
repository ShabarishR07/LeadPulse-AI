package com.marketing.leadscore;

import com.marketing.leadscore.entity.DashboardUser;
import com.marketing.leadscore.repository.DashboardUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:production_security_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "leadpulse.bootstrap-key=test-bootstrap-key",
        "leadpulse.admin.username=admin",
        "leadpulse.admin.password-bcrypt=$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
        "leadpulse.cors.allowed-origins=https://company.example"
})
@AutoConfigureMockMvc
@ActiveProfiles("production")
class ProductionSecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DashboardUserRepository dashboardUserRepository;

    @Test
    void redirectsUnauthenticatedDashboardVisitorsToLoginPage() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sign in to LeadPulse AI")));
    }

    @Test
    void accountApprovalPageIsRestrictedToAdministrators() throws Exception {
        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        mockMvc.perform(get("/admin/accounts")
                        .with(SecurityMockMvcRequestPostProcessors.user("member").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationRequiresAdminApprovalBeforeLogin() throws Exception {
        String username = "pending-" + UUID.randomUUID().toString().substring(0, 8);
        String password = "a-strong-password-123";

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Request an account")));

        mockMvc.perform(post("/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("username", username)
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        mockMvc.perform(post("/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        DashboardUser pendingUser = dashboardUserRepository.findByUsername(username).orElseThrow();
        mockMvc.perform(post("/admin/accounts/{id}/approve", pendingUser.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts?approved"));

        MockHttpSession userSession = (MockHttpSession) mockMvc.perform(post("/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/dashboard").session(userSession))
                .andExpect(status().isOk());

        mockMvc.perform(post("/logout")
                        .session(userSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void permitsAuthenticatedAdminToUseDashboardApi() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk());
    }

    @Test
    void integrationEndpointUsesApiKeyAuthenticationInsteadOfCsrfToken() throws Exception {
        mockMvc.perform(post("/api/tracking/events")
                        .contentType("application/json")
                        .content("""
                                {"eventType":"PAGE_VIEW","visitorId":"visitor-1","consentGiven":true}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rendersIntegrationSetupPageForAuthenticatedAdmin() throws Exception {
        mockMvc.perform(get("/integrations"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Keep API keys private.")));
    }
}
