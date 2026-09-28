package com.hms.hms.config;

import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            if (email != null && jwtService.isTokenValid(token, email)) {

                if (SecurityContextHolder.getContext().getAuthentication() == null) {

                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    if (Boolean.FALSE.equals(user.getActive())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("User account disabled");
                        return;
                    }

                    String role = extractRoleSafely(token);

                    List<SimpleGrantedAuthority> authorities =
                            role == null || role.isBlank()
                                    ? List.of()
                                    : List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    CustomUserDetails userDetails = new CustomUserDetails(
                            user.getId(),
                            user.getBranch() != null ? user.getBranch().getId() : null,
                            user.getEmail(),
                            user.getPassword(),
                            authorities
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    request.setAttribute("userEmail", email);
                    request.setAttribute("role", role);
                    request.setAttribute("userId", user.getId());
                    request.setAttribute(
                            "branchId",
                            user.getBranch() != null ? user.getBranch().getId() : null
                    );

                    System.out.println(
                            "AUTH SET: " + email + " ROLE: " + (role == null ? "NO_ROLE" : role)
                    );
                }
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractRoleSafely(String token) {
        try {
            return jwtService.extractRole(token);
        } catch (Exception ignored) {
            return null;
        }
    }
}
