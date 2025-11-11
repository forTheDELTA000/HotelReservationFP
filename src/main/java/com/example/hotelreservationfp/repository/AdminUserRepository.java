package com.example.hotelreservationfp.repository;

import com.example.hotelreservationfp.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    AdminUser findByEmail(String email);
}

