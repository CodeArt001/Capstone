package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwAuthFilter jwAuthFilter;

    public SecurityConfig(JwAuthFilter jwAuthFilter) {
        this.jwAuthFilter = jwAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public CORS preflight options
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Auth & API docs
                .requestMatchers("/auth/**", "/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Public GET endpoints (Supports both direct and /api prefixed routes)
                .requestMatchers(HttpMethod.GET, 
                    "/products", "/products/**", "/api/products", "/api/products/**",
                    "/categories", "/categories/**", "/api/categories", "/api/categories/**",
                    "/reviews", "/reviews/**", "/api/reviews", "/api/reviews/**"
                ).permitAll()

                // Authenticated user endpoints
                .requestMatchers(HttpMethod.POST, "/reviews/**", "/api/reviews/**").authenticated()
                .requestMatchers("/cart/**", "/api/cart/**", "/orders/**", "/api/orders/**").authenticated()

                // Admin endpoints
                .requestMatchers("/admin/**", "/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/products/**", "/api/products/**", "/categories/**", "/api/categories/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/products/**", "/api/products/**", "/categories/**", "/api/categories/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/products/**", "/api/products/**", "/categories/**", "/api/categories/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(List.of(
            "https://solemora.vercel.app",
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:3000"
        ));
        
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}