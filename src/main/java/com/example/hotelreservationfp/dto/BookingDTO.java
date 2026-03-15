package com.example.hotelreservationfp.dto;

import com.example.hotelreservationfp.entity.Booking;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDTO {
    public Integer bookingId;
    public String guestName;
    public String guestFirstName; // Added exact first name
    public String guestLastName;  // Added exact last name
    public String guestEmail;     // Added email
    public String guestPhone;     // Added phone
    public String roomNumber;
    public String roomType;       // Added actual room type name for the dropdown
    public LocalDate checkInDate;
    public LocalDate checkOutDate;
    public Integer numGuests;     // Added members
    public String message;        // Added message
    public BigDecimal totalPrice;
    public String status;

    public BookingDTO(Booking b) {
        this.bookingId = b.getBookingId();

        // Safely extract Guest info
        if (b.getGuest() != null) {
            this.guestFirstName = b.getGuest().getFirstName();
            this.guestLastName = b.getGuest().getLastName();
            this.guestName = this.guestFirstName + " " + this.guestLastName;
            this.guestEmail = b.getGuest().getEmail();
            this.guestPhone = b.getGuest().getPhone();
        }

        // Safely extract Room info
        if (b.getRoom() != null) {
            this.roomNumber = String.valueOf(b.getRoom().getRoomNumber());
            if (b.getRoom().getRoomType() != null) {
                this.roomType = b.getRoom().getRoomType().getName();
            }
        }

        this.checkInDate = b.getCheckInDate();
        this.checkOutDate = b.getCheckOutDate();
        this.numGuests = b.getNumGuests();
        this.message = b.getMessage();
        this.totalPrice = b.getTotalPrice();
        this.status = b.getStatus();
    }
}