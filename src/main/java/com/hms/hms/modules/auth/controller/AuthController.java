package com.hms.hms.modules.auth.controller;

import com.hms.hms.common.enums.Role;
import com.hms.hms.modules.auth.dto.*;
import com.hms.hms.modules.auth.service.AuthService;
import com.hms.hms.modules.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //  LOGIN
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    //  REFRESH TOKEN
    @PostMapping("/refresh-token")
    public LoginResponse refreshToken(@RequestHeader("Authorization") String refreshToken) {
        return authService.refreshToken(refreshToken);
    }
    //  LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request,
                                         @RequestHeader("Authorization") String refreshToken) {

        String email = (String) request.getAttribute("userEmail");

        authService.logout(email, refreshToken);

        return ResponseEntity.ok("Logged out successfully");
    }

    //  FORGOT PASSWORD
    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return "OTP sent to email";
    }

    //  RESET PASSWORD
    @PostMapping("/reset-password")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );
        return "Password reset successful";
    }

    //  CHANGE PASSWORD
    @PutMapping("/change-password")
    public String changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                 HttpServletRequest httpRequest) {
        String email =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();
        authService.changePassword(
                email,
                request.getOldPassword(),
                request.getNewPassword()
        );
        return "Password changed successfully";
    }

    //  CURRENT USER
    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(
            HttpServletRequest request
    ) {

        String email = (String) request.getAttribute("userEmail");

        return authService.getCurrentUser(email);
    }

    // VERIFY OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp) {
        return authService.verifyOtp(email, otp);
    }
}
