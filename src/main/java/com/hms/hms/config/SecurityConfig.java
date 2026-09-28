package com.hms.hms.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @PostConstruct
    public void init() {
        System.out.println(" SECURITY CONFIG LOADED");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:5173",
                "https://dga6e2c79xatw.cloudfront.net",
                "https://anagram-ramp-aluminum.ngrok-free.dev",
                "https://*.ngrok-free.dev",
                "https://*.ngrok.io"
        ));


        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }



    // Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    // Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http
                // Disable CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // Stateless session for JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Public + Protected APIs
                .authorizeHttpRequests(auth -> auth

                        // Static uploaded files: branch images, room images, KYC docs, menu PDFs
                        .requestMatchers(
                                "/images/**",
                                "/documents/**",
                                "/uploads/**"
                        ).permitAll()

                        // Auth public APIs
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh-token",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/verify-otp"
                        ).permitAll()

                        // Public browsing APIs
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/public/**"
                        ).permitAll()

                        // Public register API
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/public/register"
                        ).permitAll()

                        // Booking/payment actions need logged-in user token
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/public/book",
                                "/api/v1/public/pay-full/**"
                        ).authenticated()

                        // Super Admin APIs
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("SUPER_ADMIN")

                                // Owner APIs
                                .requestMatchers(
                                        "/api/v1/auth/owner/**"
                                )
                                .hasRole("OWNER")

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/v1/branches/**"
                                )
                                .hasRole("OWNER")

                            // Manager APIs
                                .requestMatchers("/api/v1/auth/manager/**")
                                .hasRole("MANAGER")

                        // Everything else needs authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}