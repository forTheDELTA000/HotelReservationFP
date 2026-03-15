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
                .csrf(csrf -> csrf.disable()) // CSRF is disabled, which is perfect for our Flutter POST requests later!
                .authorizeHttpRequests(auth -> auth

                        // Allow all static assets for your admin and main themes
                        .requestMatchers(
                                "/admin/assets/**",
                                "/main/**",
                                "/css/**",
                                "/js/**",
                                "/vendor/**",
                                "/images/**"
                        ).permitAll()

                        // Allow public client pages AND OUR NEW MOBILE API
                        .requestMatchers(
                                "/", "/index", "/about-us", "/contact",
                                "/rooms", "/services", "/bookreservation", "/process-booking",
                                "/booking-success/**",
                                "/api/**" // <--- THE MAGIC FIX IS RIGHT HERE!
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
                        .loginPage("/login")
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