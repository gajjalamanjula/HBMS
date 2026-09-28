package com.hms.hms.modules.auth.service.serviceimpl;

import com.hms.hms.common.security.RateLimitService;
import org.springframework.context.ApplicationEventPublisher;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.service.EmailService;
import com.hms.hms.config.JwtService;
import com.hms.hms.modules.auth.dto.*;
import com.hms.hms.modules.auth.entity.RefreshToken;
import com.hms.hms.modules.auth.repository.RefreshTokenRepository;
import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.branch.repository.BranchRepository;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import com.hms.hms.modules.auth.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BranchRepository branchRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RateLimitService rateLimitService;
    // LOGIN
    @Override
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail();

        String rateLimitKey = "LOGIN:" + email;

        rateLimitService.checkLimit(rateLimitKey);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        // OTP verification required except SUPER_ADMIN
        if ((user.getVerified() == null || !user.getVerified())
                && user.getRole() != Role.SUPER_ADMIN) {

            throw new CustomException(
                    "Please verify OTP first",
                    HttpStatus.FORBIDDEN
            );
        }

        // Password check
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new CustomException(
                    "Invalid credentials",
                    HttpStatus.UNAUTHORIZED
            );
        }

        rateLimitService.reset(rateLimitKey);

        String accessToken = jwtService.generateToken(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(
                user.getEmail(),
                refreshToken
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getRole().name(),
                user.getId()
        );
    }
    @Override
    public LoginResponse refreshToken(String refreshToken) {

        //  FIX 1: Safe Bearer handling
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        //  check revoked
        if (storedToken.isRevoked()) {
            throw new CustomException("Refresh token revoked", HttpStatus.UNAUTHORIZED);
        }

        //  check expiry
        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CustomException("Refresh token expired", HttpStatus.UNAUTHORIZED);
        }

        //  get user
        User user = userRepository.findByEmail(storedToken.getEmail())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        //  token rotation
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user.getEmail(), newRefreshToken);

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                user.getRole().name(),
                user.getId()
        );

    }

    @Override
    public void logout(String email, String refreshToken) {

        if (email == null || email.isBlank()) {
            throw new CustomException("Unauthorized logout request", HttpStatus.UNAUTHORIZED);
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException("Refresh token is required", HttpStatus.BAD_REQUEST);
        }

        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() ->
                        new CustomException("Invalid refresh token", HttpStatus.UNAUTHORIZED)
                );

        if (!token.getEmail().equals(email)) {
            throw new CustomException("Unauthorized logout attempt", HttpStatus.FORBIDDEN);
        }

        if (token.isRevoked()) {
            return;
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Override
    public void forgotPassword(String email) {

        String rateLimitKey = "FORGOT_PASSWORD:" + email;

        rateLimitService.checkLimit(rateLimitKey);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        String otp = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );

        user.setOtp(otp);

        user.setOtpExpiry(
                LocalDateTime.now().plusMinutes(5)
        );

        userRepository.save(user);

        emailService.sendOtpEmail(
                email,
                otp,
                user.getRole()
        );
    }
    @Override
    public void resetPassword(String email, String otp, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!otp.equals(user.getOtp())) {
            throw new CustomException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException("OTP expired", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        // CHECK OLD PASSWORD
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new CustomException("Old password is incorrect", HttpStatus.BAD_REQUEST);
        }

        // OPTIONAL: prevent same password
        if (oldPassword.equals(newPassword)) {
            throw new CustomException("New password must be different", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public CurrentUserResponse getCurrentUser(
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        return CurrentUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .branchId(
                        user.getBranch() != null
                                ? user.getBranch().getId()
                                : null
                )
                .active(user.getActive())
                .verified(user.getVerified())
                .kycVerified(user.getKycVerified())
                .build();
    }

    @Override
    public String verifyOtp(String email, String otp) {

        String rateLimitKey = "VERIFY_OTP:" + email;

        rateLimitService.checkLimit(rateLimitKey);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        if (user.getOtp() == null ||
                !user.getOtp().equals(otp)) {

            throw new CustomException(
                    "Invalid OTP",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (user.getOtpExpiry() == null ||
                user.getOtpExpiry().isBefore(
                        LocalDateTime.now()
                )) {

            throw new CustomException(
                    "OTP expired",
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setVerified(true);
        user.setActive(true);

        user.setOtp(null);
        user.setOtpExpiry(null);

        User savedUser =
                userRepository.save(user);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        savedUser.getId(),
                        savedUser.getRole().name() + " Account Created",
                        "Your account has been created and verified successfully"
                )
        );

        rateLimitService.reset(rateLimitKey);

        return "OTP verified successfully";
    }
    @Override
    @Transactional
    public String createUserByRole(
            String email,
            Role newRole,
            String name,
            String phone,
            String aadhaarNumber,
            String aadhaarPhoto,
            UUID branchId) {

        String creatorEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        System.out.println("SECURITY USER: " + creatorEmail);

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        validateRolePermission(creator.getRole(), newRole);

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            if (Boolean.TRUE.equals(existingUser.get().getVerified())) {
                throw new CustomException("User already exists", HttpStatus.CONFLICT);
            } else {
                // allow re-registration if not verified
                userRepository.delete(existingUser.get());
            }
        }

        Branch branch = resolveBranch(creator, creator.getRole(), newRole, branchId);

        String otp = generateOtp();

        User user = User.builder()
                .email(email)
                .name(name)
                .phone(phone)
                .aadhaarNumber(aadhaarNumber)
                .aadhaarUrl(aadhaarPhoto)
                .role(newRole)
                .branch(branch)
                .createdBy(creator)
                .active(false)
                .verified(false)
                .kycVerified(false)
                .password(passwordEncoder.encode("12345678")) //  default password
                .mustChangePassword(false)
                .otp(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(5))
                .build();
        userRepository.save(user);

        emailService.sendOtpEmail(
                email,
                otp,
                newRole
        );

        return "OTP sent successfully";
    }

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    private void validateRolePermission(Role creatorRole, Role newRole) {
        switch (creatorRole) {
            case SUPER_ADMIN -> {
                if (newRole != Role.OWNER)
                    throw new CustomException("SUPER_ADMIN can only create OWNER", HttpStatus.FORBIDDEN);
            }
            case OWNER -> {
                if (!(newRole == Role.MANAGER || newRole == Role.STAFF || newRole == Role.TENANT))
                    throw new CustomException("OWNER can only create MANAGER, STAFF, TENANT", HttpStatus.FORBIDDEN);
            }
            case MANAGER -> {
                if (!(newRole == Role.STAFF || newRole == Role.TENANT))
                    throw new CustomException("MANAGER can only create STAFF, TENANT", HttpStatus.FORBIDDEN);
            }
            default -> throw new CustomException("Unauthorized", HttpStatus.FORBIDDEN);
        }
    }

    private Branch resolveBranch(User creator, Role creatorRole, Role newRole, UUID branchId) {

        if (creatorRole == Role.MANAGER) {
            if (creator.getBranch() == null) {
                throw new CustomException("Manager not assigned to any branch", HttpStatus.BAD_REQUEST);
            }

            return creator.getBranch();
        }

        if (creatorRole == Role.OWNER) {
            if (!(newRole == Role.MANAGER || newRole == Role.STAFF || newRole == Role.TENANT)) {
                throw new CustomException("OWNER can only create MANAGER, STAFF, TENANT", HttpStatus.FORBIDDEN);
            }

            if (branchId == null) {
                throw new CustomException("Branch required", HttpStatus.BAD_REQUEST);
            }

            return branchRepository.findById(branchId)
                    .orElseThrow(() -> new CustomException("Branch not found", HttpStatus.NOT_FOUND));
        }

        return null;
    }

    public void saveRefreshToken(String email, String token) {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(token)
                        .email(email)
                        .expiryDate(LocalDateTime.now().plusDays(1))
                        .revoked(false)
                        .build()
        );
    }
}