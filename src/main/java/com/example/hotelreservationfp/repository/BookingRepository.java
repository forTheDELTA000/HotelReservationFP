package com.example.hotelreservationfp.repository;

import com.example.hotelreservationfp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for accessing and managing Booking entities.
 * Extends JpaRepository to provide standard CRUD operations and custom queries.
 */
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    /**
     * Retrieves all bookings along with their related guest, room, and room type details.
     * The JOIN FETCH keyword ensures all data is fetched in a single query
     * to avoid the "N+1 select" performance problem.
     */
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.guest " +
            "JOIN FETCH b.room r " +
            "JOIN FETCH r.roomType")
    List<Booking> findAllWithDetails();

    /**
     * Finds bookings that overlap with a given date range for a specific room type,
     * but excludes the current booking being updated.
     * Used to check for conflicts when editing an existing booking.
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.room.roomType.roomTypeId = :roomTypeId " +
            "AND b.checkOutDate > :checkIn " +
            "AND b.checkInDate < :checkOut " +
            "AND b.status != 'Cancelled' " +
            "AND b.bookingId != :excludeBookingId")
    List<Booking> findConflictingBookingsForUpdate(
            @Param("roomTypeId") Integer roomTypeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("excludeBookingId") Integer excludeBookingId);

    /**
     * Finds bookings that overlap with a given date range for a specific room type.
     * Used to check for room availability when creating a new booking.
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.room.roomType.roomTypeId = :roomTypeId " +
            "AND b.checkOutDate > :checkIn " +
            "AND b.checkInDate < :checkOut " +
            "AND b.status != 'Cancelled'")
    List<Booking> findConflictingBookings(
            @Param("roomTypeId") Integer roomTypeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);


    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE :today >= b.checkInDate " +
            "AND :today < b.checkOutDate " +
            "AND b.status = 'Confirmed'")
    long countActiveBookings(@Param("today") LocalDate today);

    @Query("SELECT COUNT(DISTINCT b.room.roomId) FROM Booking b " +
            "WHERE :today >= b.checkInDate " +
            "AND :today < b.checkOutDate " +
            "AND b.status != 'Cancelled'")
    long countOccupiedRoomsForToday(@Param("today") LocalDate today);
}

