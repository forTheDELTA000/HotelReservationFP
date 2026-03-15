package com.example.hotelreservationfp.dto;

import com.example.hotelreservationfp.entity.Payment;
import java.math.BigDecimal;

public class PaymentDTO {
    public Integer id;
    public BigDecimal amount;
    public String paymentDate;
    public String paymentMethod;
    public String status;
    public String guestName;
    public String bookingRef;

    public PaymentDTO(Payment payment) {
        this.id = payment.getPaymentId();
        this.amount = payment.getAmount();
        this.paymentDate = payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "N/A";
        this.paymentMethod = payment.getPaymentMethod();
        this.status = payment.getStatus();

        // Safely pull the Guest Name and Booking ID from the related Booking
        if (payment.getBooking() != null) {
            this.bookingRef = "BKG-" + payment.getBooking().getBookingId();
            if (payment.getBooking().getGuest() != null) {
                this.guestName = payment.getBooking().getGuest().getFirstName() + " " + payment.getBooking().getGuest().getLastName();
            } else {
                this.guestName = "Unknown Guest";
            }
        }
    }

    public Integer getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentDate() { return paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public String getGuestName() { return guestName; }
    public String getBookingRef() { return bookingRef; }
}