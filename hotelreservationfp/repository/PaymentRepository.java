package com.example.hotelreservationfp.repository;

import com.example.hotelreservationfp.entity.Payment;
import com.example.hotelreservationfp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import this
import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findByBooking(Booking booking);
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'Paid'")
    BigDecimal findTotalRevenue();

    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.booking b " +
            "JOIN FETCH b.guest")
    List<Payment> findAllWithBookingAndGuest();
}
