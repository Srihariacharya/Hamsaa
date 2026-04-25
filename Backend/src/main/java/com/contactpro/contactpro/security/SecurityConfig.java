package com.contactpro.contactpro.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
// provides password hashing service for authentication

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // disables CSRF for API testing
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                            "/swagger-ui.html",
                            "/api/integrations/google/auth-url",
                            "/api/integrations/google/callback"
                        ).permitAll() // allow swagger endpoints

                        .requestMatchers("/api/users/**").permitAll() // allow login/register

                        .anyRequest().permitAll() // allow all APIs for now
                );

        return http.build();


    }
}