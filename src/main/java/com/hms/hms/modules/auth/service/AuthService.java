package com.hms.hms.modules.auth.service;



import com.hms.hms.common.enums.Role;
import com.hms.hms.modules.auth.dto.CurrentUserResponse;
import com.hms.hms.modules.auth.dto.LoginRequest;
import com.hms.hms.modules.auth.dto.LoginResponse;
import com.hms.hms.modules.user.entity.User;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;


public interface AuthService {

    //  LOGIN
    LoginResponse login(LoginRequest request);

    //  REFRESH TOKEN
    LoginResponse refreshToken(String refreshToken);

    //  LOGOUT
    void logout(String email, String refreshToken);

    //  FORGOT PASSWORD (SEND OTP)
    void forgotPassword(String email);

    //  RESET PASSWORD (WITH OTP)
    void resetPassword(String email, String otp, String newPassword);

    //  CHANGE PASSWORD
    public void changePassword(String email, String oldPassword, String newPassword);

    //  GET CURRENT USER
    CurrentUserResponse getCurrentUser(String email);



    String createUserByRole(
            String email,
            Role role,
            String name,
            String phone,
            String aadhaarNumber,
            String aadhaarPhoto,
            UUID branchId
    );
    String verifyOtp(String email, String otp);

}

