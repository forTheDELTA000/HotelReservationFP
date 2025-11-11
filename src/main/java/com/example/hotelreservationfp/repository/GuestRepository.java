package com.example.hotelreservationfp.repository;

import com.example.hotelreservationfp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Integer> {
    Guest findByEmail(String email);
}
