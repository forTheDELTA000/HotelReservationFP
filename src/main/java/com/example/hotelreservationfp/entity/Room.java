package com.example.hotelreservationfp.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference; // 1. Add this import
import com.fasterxml.jackson.annotation.JsonIgnore;        // 2. Add this import
import java.util.List;

@Entity
@Table(name = "room_tbl")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomId;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    @JsonBackReference // 3. Add this annotation here!
    private RoomType roomType;

    @Column(nullable = false, length = 10)
    private String roomNumber;

    @Column(nullable = false, length = 50)
    private String status;

    @OneToMany(mappedBy = "room")
    @JsonIgnore // 4. Add this here to prevent future loops with Bookings
    private List<Booking> bookings;

    // ... Getters and Setters remain exactly the same ...
    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}