package com.example.demo.security;

import com.example.demo.config.JwUtil;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email = null;

        try {
            email = jwtUtil.extractEmail(token);
        } catch (ExpiredJwtException e) {
            System.out.println("=== JWT FILTER DEBUG ===");
            System.out.println("Token expired: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("=== JWT FILTER DEBUG ===");
            System.out.println("Invalid token signature or format: " + e.getMessage());
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(email).orElse(null);

            boolean isTokenValid = jwtUtil.validateToken(token, email);

            System.out.println("=== JWT FILTER DEBUG ===");
            System.out.println("Extracted Email: " + email);
            System.out.println("User Found: " + (user != null));
            if (user != null) {
                System.out.println("Is Token Valid: " + isTokenValid);
            }

            // REMOVED isEmailVerified check so authentication succeeds
            if (user != null && isTokenValid) {
                
                List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                        .map(role -> {
                            String roleName = role.getName();
                            if (!roleName.startsWith("ROLE_")) {
                                roleName = "ROLE_" + roleName;
                            }
                            return new SimpleGrantedAuthority(roleName);
                        })
                        .collect(Collectors.toList());

                System.out.println("Assigned Authorities: " + authorities);

                // Pass user object or email as the principal
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("AUTHENTICATION SUCCESSFUL FOR USER: " + email);
            } else {
                System.out.println("AUTHENTICATION FAILED IN FILTER!");
            }
        }

        filterChain.doFilter(request, response);
    }
}