package com.example.hotelreservationfp.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "room_tbl")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomId;

    // This is the "Foreign Key"
    // Many Rooms can belong to One RoomType
    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false, length = 10)
    private String roomNumber;

    @Column(nullable = false, length = 50)
    private String status; // e.g., "Available", "Under Maintenance"

    // One Room can have Many Bookings
    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;

    // Getters and Setters
    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}
