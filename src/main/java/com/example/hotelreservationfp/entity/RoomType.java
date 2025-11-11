package com.example.hotelreservationfp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal; // Use BigDecimal for money
import java.util.List;

@Entity
@Table(name = "room_type_tbl")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomTypeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT") // For long descriptions
    private String description;

    @Column(nullable = false)
    private BigDecimal pricePerNight; // Use BigDecimal for currency

    @Column(nullable = false)
    private Integer maxGuests;

    private String imageUrl; // Path to the photo

    // This links to the physical rooms
    // One RoomType can have Many Rooms
    @OneToMany(mappedBy = "roomType")
    private List<Room> rooms;

    // Getters and Setters
    public Integer getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Integer getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(Integer maxGuests) {
        this.maxGuests = maxGuests;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
