//package com.hms.hms.config;
//
//import com.hms.hms.modules.user.entity.User;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.security.Key;
//import java.util.Date;
//
//@Service
//public class JwtService {
//    @Value("${jwt.secret}")
//    private String secret;
//
//    // Token validity
//    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 24; // 1 day
//    private static final long REFRESH_TOKEN_VALIDITY =
//            1000L * 60 * 60 * 24 * 3; // 3 days
//
//    //  Signing Key
//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(secret.getBytes());
//    }
//
//    // =========================
//    // GENERATE ACCESS TOKEN
//    // =========================
//    public String generateToken(User user) {
//
//        return Jwts.builder()
//                .setSubject(user.getEmail())
//                .claim("role", user.getRole().name())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // =========================
//    //  GENERATE REFRESH TOKEN
//    // =========================
//    public String generateRefreshToken(User user) {
//
//        return Jwts.builder()
//                .setSubject(user.getEmail())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // =========================
//    //  EXTRACT EMAIL
//    // =========================
//    public String extractEmail(String token) {
//        return extractClaims(token).getSubject();
//    }
//
//    // =========================
//    //  EXTRACT ROLE
//    // =========================
//    public String extractRole(String token) {
//        return extractClaims(token).get("role", String.class);
//    }
//
//    // =========================
//    //  VALIDATE TOKEN
//    // =========================
//    public boolean isTokenValid(String token, String email) {
//        final String extractedEmail = extractEmail(token);
//        return (extractedEmail.equals(email) && !isTokenExpired(token));
//    }
//
//    // =========================
//    // CHECK EXPIRY
//    // =========================
//    boolean isTokenExpired(String token) {
//        return extractClaims(token).getExpiration().before(new Date());
//    }
//
//    // =========================
//    //  EXTRACT ALL CLAIMS
//    // =========================
//    private Claims extractClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//}
package com.hms.hms.config;

import com.hms.hms.modules.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // Access token validity: 1 day
    private static final long ACCESS_TOKEN_VALIDITY =
            1000L * 60 * 60 * 24;

    // Refresh token validity: 3 days
    private static final long REFRESH_TOKEN_VALIDITY =
            1000L * 60 * 60 * 24 * 3;

    // Signing Key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Generate Access Token
    public String generateToken(User user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + ACCESS_TOKEN_VALIDITY)
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate Refresh Token
    public String generateRefreshToken(User user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + REFRESH_TOKEN_VALIDITY)
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract Email
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // Extract Role
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // Validate Token
    public boolean isTokenValid(String token, String email) {
        final String extractedEmail = extractEmail(token);

        return extractedEmail.equals(email)
                && !isTokenExpired(token);
    }

    // Check Expiry
    boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // Extract Claims
    private Claims extractClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}