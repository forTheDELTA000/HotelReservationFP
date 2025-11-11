package com.example.hotelreservationfp.controller;

import com.example.hotelreservationfp.dto.BookingRequest;
import com.example.hotelreservationfp.entity.*;
import com.example.hotelreservationfp.repository.*;
import com.example.hotelreservationfp.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private AdminUserRepository adminUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private GuestRepository guestRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BookingService bookingService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @PostMapping("/register-admin")
    public String registerAdmin(Model model) {
        // Registration logic here if needed
        return "redirect:/login";
    }

    // --- Room Type Methods ---
    @PostMapping("/save-room-type")
    public String saveRoomType(@ModelAttribute("roomType") RoomType roomType) {
        roomTypeRepository.save(roomType);
        return "redirect:/all-rooms";
    }

    @GetMapping("/delete-room/{id}")
    public String deleteRoomType(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            roomTypeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete. This Room Type is still linked to Physical Rooms.");
        }
        return "redirect:/all-rooms";
    }

    // --- Physical Room Methods ---
    @PostMapping("/save-physical-room")
    public String savePhysicalRoom(@ModelAttribute("room") Room room) {
        roomRepository.save(room);
        return "redirect:/physical-rooms";
    }

    @GetMapping("/delete-physical-room/{id}")
    public String deletePhysicalRoom(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            roomRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete. This Room is still linked to Bookings.");
        }
        return "redirect:/physical-rooms";
    }

    // --- Guest Methods ---
    @PostMapping("/save-customer")
    public String saveCustomer(@ModelAttribute("guest") Guest guest) {
        guestRepository.save(guest);
        return "redirect:/all-customer";
    }

    @GetMapping("/delete-customer/{id}")
    public String deleteCustomer(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            guestRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete. This Guest is still linked to Bookings.");
        }
        return "redirect:/all-customer";
    }

    // --- Booking Methods ---
    @PostMapping("/save-booking")
    public String saveBooking(@ModelAttribute("bookingRequest") BookingRequest request,
                              RedirectAttributes redirectAttributes) {

        LocalDate checkIn = LocalDate.parse(request.getCheckInDate(), formatter);
        LocalDate checkOut = LocalDate.parse(request.getCheckOutDate(), formatter);

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId()).orElse(null);
        if (roomType == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid Room Type selected.");
            return "redirect:/add-booking";
        }

        if (request.getNumGuests() > roomType.getMaxGuests()) {
            redirectAttributes.addFlashAttribute("error", "Number of guests (" + request.getNumGuests() +
                    ") exceeds the maximum for this room (" + roomType.getMaxGuests() + ").");
            return "redirect:/add-booking";
        }

        Guest guest = guestRepository.findByEmail(request.getGuestEmail());
        if (guest == null) guest = new Guest();

        guest.setFirstName(request.getGuestFirstName());
        guest.setLastName(request.getGuestLastName());
        guest.setEmail(request.getGuestEmail());
        guest.setPhone(request.getGuestPhone());
        guestRepository.save(guest);

        Room availableRoom = bookingService.findAvailableRoom(
                request.getRoomTypeId(), checkIn, checkOut);

        if (availableRoom == null) {
            redirectAttributes.addFlashAttribute("error", "No rooms of that type are available for the selected dates.");
            return "redirect:/add-booking";
        }

        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal totalPrice = availableRoom.getRoomType().getPricePerNight().multiply(new BigDecimal(numberOfNights));

        Booking booking = new Booking();
        booking.setGuest(guest);
        booking.setRoom(availableRoom);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumGuests(request.getNumGuests());
        booking.setTotalPrice(totalPrice);
        booking.setStatus("Confirmed");
        booking.setBookingDate(LocalDateTime.now());
        booking.setMessage(request.getMessage());
        bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(request.getPaymentStatus());
        if ("Paid".equals(request.getPaymentStatus())) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        paymentRepository.save(payment);

        return "redirect:/all-booking";
    }

    @PostMapping("/update-booking/{id}")
    public String updateBooking(@PathVariable("id") Integer bookingId,
                                @ModelAttribute("bookingRequest") BookingRequest request,
                                RedirectAttributes redirectAttributes) {

        LocalDate checkIn = LocalDate.parse(request.getCheckInDate(), formatter);
        LocalDate checkOut = LocalDate.parse(request.getCheckOutDate(), formatter);

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId()).orElse(null);
        if (roomType == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid Room Type selected.");
            return "redirect:/edit-booking/" + bookingId;
        }

        if (request.getNumGuests() > roomType.getMaxGuests()) {
            redirectAttributes.addFlashAttribute("error", "Number of guests (" + request.getNumGuests() +
                    ") exceeds the maximum for this room (" + roomType.getMaxGuests() + ").");
            return "redirect:/edit-booking/" + bookingId;
        }

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) return "redirect:/all-booking";

        Booking booking = bookingOptional.get();
        Guest guest = booking.getGuest();
        guest.setFirstName(request.getGuestFirstName());
        guest.setLastName(request.getGuestLastName());
        guest.setEmail(request.getGuestEmail());
        guest.setPhone(request.getGuestPhone());
        guestRepository.save(guest);

        boolean roomChanged = !booking.getRoom().getRoomType().getRoomTypeId().equals(request.getRoomTypeId());
        boolean datesChanged = !booking.getCheckInDate().equals(checkIn) || !booking.getCheckOutDate().equals(checkOut);
        Room assignedRoom = booking.getRoom();

        if (roomChanged || datesChanged) {
            Room newRoom = bookingService.findAvailableRoomForUpdate(
                    request.getRoomTypeId(), checkIn, checkOut, bookingId);

            if (newRoom == null) {
                redirectAttributes.addFlashAttribute("error", "No rooms of that type are available for the new dates.");
                return "redirect:/edit-booking/" + bookingId;
            }
            assignedRoom = newRoom;
        }

        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal totalPrice = assignedRoom.getRoomType().getPricePerNight().multiply(new BigDecimal(numberOfNights));

        booking.setGuest(guest);
        booking.setRoom(assignedRoom);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumGuests(request.getNumGuests());
        booking.setTotalPrice(totalPrice);
        booking.setMessage(request.getMessage());
        bookingRepository.save(booking);

        Payment payment = paymentRepository.findByBooking(booking);
        if (payment == null) payment = new Payment();

        payment.setBooking(booking);
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(request.getPaymentStatus());

        if ("Paid".equals(request.getPaymentStatus()) && payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        paymentRepository.save(payment);
        return "redirect:/all-booking";
    }
}
