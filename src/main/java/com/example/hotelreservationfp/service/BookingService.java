package com.example.hotelreservationfp.service;

import com.example.hotelreservationfp.entity.Booking;
import com.example.hotelreservationfp.entity.Room;
import com.example.hotelreservationfp.repository.BookingRepository;
import com.example.hotelreservationfp.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * This is for NEW bookings.
     */
    public Room findAvailableRoom(Integer roomTypeId, LocalDate checkIn, LocalDate checkOut) {

        List<Room> allRoomsOfType = roomRepository.findByRoomType_RoomTypeId(roomTypeId);

        // Calls the simple query
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(roomTypeId, checkIn, checkOut);

        Set<Integer> bookedRoomIds = conflictingBookings.stream()
                .map(booking -> booking.getRoom().getRoomId())
                .collect(Collectors.toSet());

        for (Room room : allRoomsOfType) {
            if (!bookedRoomIds.contains(room.getRoomId()) && "Available".equals(room.getStatus())) {
                return room;
            }
        }
        return null;
    }

    // --- FIX FOR BUG 1: This is the new method for UPDATING bookings ---
    /**
     * This is for EDITING bookings.
     * It excludes the booking we are editing from the conflict check.
     */
    public Room findAvailableRoomForUpdate(Integer roomTypeId, LocalDate checkIn, LocalDate checkOut, Integer excludeBookingId) {

        List<Room> allRoomsOfType = roomRepository.findByRoomType_RoomTypeId(roomTypeId);

        // Calls the new, "smarter" query
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookingsForUpdate(
                roomTypeId, checkIn, checkOut, excludeBookingId
        );

        Set<Integer> bookedRoomIds = conflictingBookings.stream()
                .map(booking -> booking.getRoom().getRoomId())
                .collect(Collectors.toSet());

        for (Room room : allRoomsOfType) {
            if (!bookedRoomIds.contains(room.getRoomId()) && "Available".equals(room.getStatus())) {
                return room;
            }
        }
        return null;
    }
}