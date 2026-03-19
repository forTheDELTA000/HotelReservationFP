package com.example.hotelreservationfp.controller;

import com.example.hotelreservationfp.dto.BookingDTO;
import com.example.hotelreservationfp.dto.CustomerDTO;
import com.example.hotelreservationfp.dto.RoomDTO;
import com.example.hotelreservationfp.dto.RoomTypeDTO;
import com.example.hotelreservationfp.dto.PaymentDTO;
import com.example.hotelreservationfp.dto.BookingRequest;
import com.example.hotelreservationfp.entity.*;
import com.example.hotelreservationfp.repository.*;
import com.example.hotelreservationfp.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api") // All mobile requests must start with /api
public class MobileApiController {

    @Autowired
    private RoomTypeRepository roomTypeRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private GuestRepository guestRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BookingService bookingService;

    // We use the same date formatter your web controller uses
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // --- 1. GET ALL ROOM TYPES ---
    // Flutter will call: GET http://10.0.2.2:8080/api/rooms
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomType>> getMobileRooms() {
        List<RoomType> rooms = roomTypeRepository.findAll();
        return ResponseEntity.ok(rooms);
    }

    // --- 2. SUBMIT A BOOKING ---
    // Flutter will call: POST http://10.0.2.2:8080/api/book
    @PostMapping("/book")
    public ResponseEntity<String> processMobileBooking(@RequestBody BookingRequest request) {

        try {
            LocalDate checkIn = LocalDate.parse(request.getCheckInDate(), formatter);
            LocalDate checkOut = LocalDate.parse(request.getCheckOutDate(), formatter);

            // 1. Verify Room Type
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId()).orElse(null);
            if (roomType == null) {
                return ResponseEntity.badRequest().body("Invalid Room Selected");
            }

            // 2. Handle the Guest (This is where Flutter's SQLite data comes in!)
            Guest guest = guestRepository.findByEmail(request.getGuestEmail());
            if (guest == null) { guest = new Guest(); }
            guest.setFirstName(request.getGuestFirstName());
            guest.setLastName(request.getGuestLastName());
            guest.setEmail(request.getGuestEmail());
            guest.setPhone(request.getGuestPhone());
            guestRepository.save(guest);

            // 3. Find Available Physical Room
            Room availableRoom = bookingService.findAvailableRoom(request.getRoomTypeId(), checkIn, checkOut);
            if (availableRoom == null) {
                return ResponseEntity.badRequest().body("Sorry! No rooms available for those dates.");
            }

            // 4. Calculate Price
            long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (numberOfNights < 1) numberOfNights = 1;
            BigDecimal totalPrice = roomType.getPricePerNight().multiply(new BigDecimal(numberOfNights));

            // 5. Save the Booking
            Booking booking = new Booking();
            booking.setGuest(guest);
            booking.setRoom(availableRoom);
            booking.setCheckInDate(checkIn);
            booking.setCheckOutDate(checkOut);
            booking.setNumGuests(request.getNumGuests());
            booking.setTotalPrice(totalPrice);
            booking.setStatus("Pending");
            booking.setBookingDate(LocalDateTime.now());
            // Safe check for message since it might be null from mobile
            booking.setMessage(request.getMessage() != null ? request.getMessage() : "Mobile Booking");
            bookingRepository.save(booking);

            // 6. Update Physical Room Status
            availableRoom.setStatus("Occupied");
            roomRepository.save(availableRoom);

            // 7. Save the Payment Intent
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(totalPrice);
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setStatus("Pending");
            paymentRepository.save(payment);

            // Tell Flutter the booking was a success!
            return ResponseEntity.ok("Booking Successful! ID: " + booking.getBookingId());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing booking: " + e.getMessage());
        }
    }

    @GetMapping("/admin/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total Bookings
        stats.put("totalBookings", bookingRepository.count());

        // Total Guests
        stats.put("totalGuests", guestRepository.count());

        // Available Rooms (Using your exact WebController logic)
        stats.put("availableRooms", roomRepository.countByStatus("Available"));

        // Total Revenue
        BigDecimal totalRevenue = paymentRepository.findTotalRevenue();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        return ResponseEntity.ok(stats);
    }

    // 2. Get All Bookings for Admin List
    // Flutter will call: GET http://10.222.246.184:8080/api/admin/bookings
    @GetMapping("/admin/bookings")
    public ResponseEntity<List<BookingDTO>> getAdminBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        List<BookingDTO> dtos = bookings.stream()
                .map(BookingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // 1. UPDATE BOOKING STATUS (Edit)
// Flutter will call: PUT http://10.222.246.184:8080/api/admin/bookings/6/status?newStatus=Cancelled
    @PutMapping("/admin/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Integer id, @RequestParam String newStatus) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus(newStatus);
            bookingRepository.save(booking);
            return ResponseEntity.ok("Status updated successfully");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/bookings/{id}")
    public ResponseEntity<?> updateBookingFull(@PathVariable Integer id, @RequestBody Map<String, Object> updates) {
        return bookingRepository.findById(id).map(booking -> {
            // 1. Update Guest safely
            Guest guest = booking.getGuest();
            if (updates.containsKey("guestFirstName")) guest.setFirstName((String) updates.get("guestFirstName"));
            if (updates.containsKey("guestLastName")) guest.setLastName((String) updates.get("guestLastName"));
            if (updates.containsKey("guestEmail")) guest.setEmail((String) updates.get("guestEmail"));
            if (updates.containsKey("guestPhone")) guest.setPhone((String) updates.get("guestPhone"));
            guestRepository.save(guest);

            // 2. THE FIX: Smart Status Translator
            if (updates.containsKey("status")) {
                String flutterStatus = (String) updates.get("status"); // Flutter sends "Paid" or "Pending"

                // A. Update the Payment table for accurate Revenue calculation
                Payment payment = paymentRepository.findByBooking(booking);
                if (payment != null) {
                    payment.setStatus(flutterStatus);
                    paymentRepository.save(payment);
                }

                // B. Sync the Booking table so Desktop and Mobile match
                if ("Paid".equalsIgnoreCase(flutterStatus)) {
                    booking.setStatus("Confirmed"); // If paid, booking is confirmed
                } else if ("Pending".equalsIgnoreCase(flutterStatus)) {
                    booking.setStatus("Pending");   // If payment is pending, booking is pending
                } else {
                    booking.setStatus(flutterStatus); // Fallback
                }
            }

            // 3. Update Dates and Message
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (updates.containsKey("checkInDate")) booking.setCheckInDate(LocalDate.parse((String) updates.get("checkInDate"), formatter));
            if (updates.containsKey("checkOutDate")) booking.setCheckOutDate(LocalDate.parse((String) updates.get("checkOutDate"), formatter));
            if (updates.containsKey("message")) booking.setMessage((String) updates.get("message"));

            bookingRepository.save(booking);
            return ResponseEntity.ok("Booking fully updated by ID.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // 2. DELETE BOOKING
// Flutter will call: DELETE http://10.222.246.184:8080/api/admin/bookings/6
    @DeleteMapping("/admin/bookings/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Integer id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return ResponseEntity.ok("Booking deleted");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/admin/bookings")
    public ResponseEntity<?> adminCreateBooking(@RequestBody BookingDTO dto) {
        // Logic to manually save a booking via repository
        return ResponseEntity.ok("Booking Created");
    }

    // 1. READ: Send all customers to Flutter
    // Flutter will call: GET http://<your-ip>:8080/api/admin/customers
    @GetMapping("/admin/customers")
    public ResponseEntity<List<CustomerDTO>> getAdminCustomers() {
        List<Guest> guests = guestRepository.findAll();
        List<CustomerDTO> dtos = guests.stream()
                .map(CustomerDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // 2. CREATE: Add a new customer from Flutter
    @PostMapping("/admin/customers")
    public ResponseEntity<?> addCustomer(@RequestBody Map<String, String> payload) {
        Guest newGuest = new Guest();
        newGuest.setFirstName(payload.get("firstName"));
        newGuest.setLastName(payload.get("lastName"));
        newGuest.setEmail(payload.get("email"));
        newGuest.setPhone(payload.get("phone"));

        guestRepository.save(newGuest);
        return ResponseEntity.ok("Customer added successfully");
    }

    // 3. UPDATE: Edit an existing customer
    @PutMapping("/admin/customers/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        return guestRepository.findById(id).map(guest -> {
            if (payload.containsKey("firstName")) guest.setFirstName(payload.get("firstName"));
            if (payload.containsKey("lastName")) guest.setLastName(payload.get("lastName"));
            if (payload.containsKey("email")) guest.setEmail(payload.get("email"));
            if (payload.containsKey("phone")) guest.setPhone(payload.get("phone"));

            guestRepository.save(guest);
            return ResponseEntity.ok("Customer updated successfully");
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE: Remove a customer
    @DeleteMapping("/admin/customers/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Integer id) {
        if (guestRepository.existsById(id)) {
            guestRepository.deleteById(id);
            return ResponseEntity.ok("Customer deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // --- PHYSICAL ROOMS CRUD ENDPOINTS ---

    // 1. READ: Get all physical rooms
    @GetMapping("/admin/physical-rooms")
    public ResponseEntity<List<RoomDTO>> getAdminPhysicalRooms() {
        List<RoomDTO> rooms = roomRepository.findAll().stream()
                .map(RoomDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    // 2. CREATE: Add a new physical room
    @PostMapping("/admin/physical-rooms")
    public ResponseEntity<?> addPhysicalRoom(@RequestBody Map<String, String> payload) {
        Room newRoom = new Room();
        newRoom.setRoomNumber(String.valueOf(Integer.parseInt(payload.get("roomNumber")))); // No more red line!
        newRoom.setStatus(payload.get("status"));

        Integer typeId = Integer.parseInt(payload.get("roomTypeId"));
        RoomType type = roomTypeRepository.findById(typeId).orElse(null);
        if (type != null) newRoom.setRoomType(type);

        roomRepository.save(newRoom);
        return ResponseEntity.ok("Room added successfully");
    }

    // 3. UPDATE: Edit a physical room
    @PutMapping("/admin/physical-rooms/{id}")
    public ResponseEntity<?> updatePhysicalRoom(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        return roomRepository.findById(id).map(room -> {
            if (payload.containsKey("roomNumber")) {
                room.setRoomNumber(String.valueOf(Integer.parseInt(payload.get("roomNumber")))); // No more red line!
            }
            if (payload.containsKey("status")) {
                room.setStatus(payload.get("status"));
            }
            if (payload.containsKey("roomTypeId")) {
                Integer typeId = Integer.parseInt(payload.get("roomTypeId"));
                RoomType type = roomTypeRepository.findById(typeId).orElse(null);
                if (type != null) room.setRoomType(type);
            }

            roomRepository.save(room);
            return ResponseEntity.ok("Room updated successfully");
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE: Remove a physical room
    @DeleteMapping("/admin/physical-rooms/{id}")
    public ResponseEntity<?> deletePhysicalRoom(@PathVariable Integer id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return ResponseEntity.ok("Room deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // --- ROOM TYPES CRUD ENDPOINTS ---

    @GetMapping("/admin/room-types")
    public ResponseEntity<List<RoomTypeDTO>> getAdminRoomTypes() {
        List<RoomTypeDTO> types = roomTypeRepository.findAll().stream()
                .map(RoomTypeDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }

    @PostMapping("/admin/room-types")
    public ResponseEntity<?> addRoomType(@RequestBody Map<String, String> payload) {
        RoomType type = new RoomType();
        type.setName(payload.get("name"));
        type.setPricePerNight(new BigDecimal(payload.get("price")));
        type.setMaxGuests(Integer.parseInt(payload.get("maxGuests")));
        type.setDescription(payload.get("description"));
        type.setImageUrl(payload.get("imageUrl"));

        roomTypeRepository.save(type);
        return ResponseEntity.ok("Room Type added successfully");
    }

    @PutMapping("/admin/room-types/{id}")
    public ResponseEntity<?> updateRoomType(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        return roomTypeRepository.findById(id).map(type -> {
            if (payload.containsKey("name")) type.setName(payload.get("name"));
            if (payload.containsKey("price")) type.setPricePerNight(new BigDecimal(payload.get("price")));
            if (payload.containsKey("maxGuests")) type.setMaxGuests(Integer.parseInt(payload.get("maxGuests")));
            if (payload.containsKey("description")) type.setDescription(payload.get("description"));
            if (payload.containsKey("imageUrl")) type.setImageUrl(payload.get("imageUrl"));

            roomTypeRepository.save(type);
            return ResponseEntity.ok("Room Type updated successfully");
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/room-types/{id}")
    public ResponseEntity<?> deleteRoomType(@PathVariable Integer id) {
        if (roomTypeRepository.existsById(id)) {
            roomTypeRepository.deleteById(id);
            return ResponseEntity.ok("Room Type deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/admin/payments")
    public ResponseEntity<List<PaymentDTO>> getAdminPayments() {
        List<PaymentDTO> payments = paymentRepository.findAll().stream()
                .map(PaymentDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }
}