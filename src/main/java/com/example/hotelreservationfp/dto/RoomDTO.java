package com.example.hotelreservationfp.dto;

import com.example.hotelreservationfp.entity.Room;

public class RoomDTO {
    public Integer id;
    public String roomNumber;
    public String roomTypeName;
    public Integer roomTypeId;
    public String status;

    public RoomDTO(Room room) {
        this.id = room.getRoomId();
        this.roomNumber = String.valueOf(room.getRoomNumber());
        this.status = room.getStatus();

        if (room.getRoomType() != null) {
            this.roomTypeName = room.getRoomType().getName();
            this.roomTypeId = room.getRoomType().getRoomTypeId();
        }
    }

    // Getters
    public Integer getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomTypeName() { return roomTypeName; }
    public Integer getRoomTypeId() { return roomTypeId; }
    public String getStatus() { return status; }
}