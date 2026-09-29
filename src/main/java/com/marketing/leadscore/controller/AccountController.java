package com.marketing.leadscore.controller;

import com.marketing.leadscore.service.DashboardUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

@Controller
@Profile("production")
public class AccountController {

    private final DashboardUserService userService;
    private final String administratorUsername;

    public AccountController(DashboardUserService userService,
                             @Value("${leadpulse.admin.username}") String administratorUsername) {
        this.userService = userService;
        this.administratorUsername = administratorUsername;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registration(Model model) {
        if (!model.containsAttribute("registration")) {
            model.addAttribute("registration", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registration") RegistrationForm form,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Check the submitted values and try again.");
            return "register";
        }
        try {
            userService.register(form.getUsername(), form.getPassword(), form.getConfirmPassword(),
                    administratorUsername);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/admin/accounts")
    public String pendingAccounts(Model model) {
        model.addAttribute("pendingUsers", userService.getPendingUsers());
        return "admin-accounts";
    }

    @PostMapping("/admin/accounts/{id}/approve")
    public String approveAccount(@PathVariable Long id) {
        userService.approve(id);
        return "redirect:/admin/accounts?approved";
    }

    public static class RegistrationForm {
        @NotBlank
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank
        @Size(min = 12, max = 72)
        private String password;

        @NotBlank
        @Size(min = 12, max = 72)
        private String confirmPassword;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
