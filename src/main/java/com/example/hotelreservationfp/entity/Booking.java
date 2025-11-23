package com.example.hotelreservationfp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a hotel room booking record.
 * Each booking links a guest, a room, and may have a related payment.
 */
@Entity
@Table(name = "booking_tbl")
public class Booking {

    // Primary key for the booking table
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookingId;

    // Relationship to the guest who made the booking
    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    // Relationship to the booked room
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // One-to-one relationship with payment details (if payment is made)
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    // Date when the guest will check in
    @Column(nullable = false)
    private LocalDate checkInDate;

    // Date when the guest will check out
    @Column(nullable = false)
    private LocalDate checkOutDate;

    // Number of guests for this booking
    @Column(nullable = false)
    private Integer numGuests;

    // Total cost of the booking
    private BigDecimal totalPrice;

    // Status of the booking (e.g., Pending, Confirmed, Cancelled)
    @Column(nullable = false, length = 50)
    private String status;

    // Date and time when the booking was created
    private LocalDateTime bookingDate;

    // Optional message or note from the guest (stored as long text)
    @Column(columnDefinition = "TEXT")
    private String message;

    // Getters and setters for all fields

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getNumGuests() {
        return numGuests;
    }

    public void setNumGuests(Integer numGuests) {
        this.numGuests = numGuests;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
