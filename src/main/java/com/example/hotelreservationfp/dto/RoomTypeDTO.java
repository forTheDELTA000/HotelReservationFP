package com.example.hotelreservationfp.dto;

import com.example.hotelreservationfp.entity.RoomType;
import java.math.BigDecimal;

public class RoomTypeDTO {
    public Integer id;
    public String name;
    public BigDecimal price;
    public Integer maxGuests;
    public String description;
    public String imageUrl;

    public RoomTypeDTO(RoomType type) {
        this.id = type.getRoomTypeId();
        this.name = type.getName();
        this.price = type.getPricePerNight();
        this.maxGuests = type.getMaxGuests(); // Assuming your entity uses 'capacity' or 'maxGuests'
        this.description = type.getDescription();
        this.imageUrl = type.getImageUrl(); // Assuming you have an image URL field
    }

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public Integer getMaxGuests() { return maxGuests; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}