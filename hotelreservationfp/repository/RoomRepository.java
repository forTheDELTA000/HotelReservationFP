package com.example.hotelreservationfp.repository;

import com.example.hotelreservationfp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    @Query("SELECT r FROM Room r JOIN FETCH r.roomType")
    List<Room> findAllWithRoomType();
    List<Room> findByRoomType_RoomTypeId(Integer roomTypeId);
    long countByStatus(String status);
}