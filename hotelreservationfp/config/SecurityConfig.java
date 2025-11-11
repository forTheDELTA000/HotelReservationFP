package com.example.hotelreservationfp.config; // Or your config package

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Allow all static assets for your admin and main themes
                        .requestMatchers(
                                "/admin/assets/**", // Allow new admin theme assets
                                "/main/**",
                                "/css/**",
                                "/js/**",
                                "/vendor/**",
                                "/images/**"
                        ).permitAll()

                        // Allow public client pages
                        .requestMatchers(
                                "/", "/index", "/about-us", "/contact",
                                "/rooms", "/services"
                        ).permitAll()

                        // Allow public admin utility pages
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/forgot-password",
                                "/register-admin"
                        ).permitAll()

                        // Protect all other pages
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // This is your login page's URL

                        // --- THIS IS THE FIX FOR YOUR REQUEST ---
                        // After a successful login, send the user to /dashboard
                        .defaultSuccessUrl("/dashboard", true)

                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}