package com.hms.hms.common.service;

import com.hms.hms.common.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp, Role role) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);

        message.setSubject(role.name() + " Account Verification OTP");

        message.setText(
                "Hello " + role.name() + ",\n\n" +
                        "Your OTP is: " + otp + "\n" +
                        "Valid for 5 minutes.\n\n" +
                        "Thank you."
        );

        mailSender.send(message);
    }
}