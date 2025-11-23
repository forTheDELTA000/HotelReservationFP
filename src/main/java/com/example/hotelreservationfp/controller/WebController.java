package com.example.hotelreservationfp.controller;

import com.example.hotelreservationfp.dto.BookingRequest;
import com.example.hotelreservationfp.entity.*;
import com.example.hotelreservationfp.repository.*;
import com.example.hotelreservationfp.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
public class WebController {

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

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter adminFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // --- Main/Customer Pages ---
    @GetMapping("/")
    public String redirectToAbout() { return "main/index"; }
    @GetMapping("/about-us")
    public String getAboutUsPage() { return "main/about-us"; }

    @GetMapping("/index")
    public String getIndexPage(Model model) {
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        return "main/index";
    }

    @GetMapping("/rooms")
    public String getRoomsPage(Model model) {
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        return "main/rooms";
    }

    @GetMapping("/services")
    public String getServicesPage() { return "main/services"; }

    // --- CUSTOMER RESERVATION LOGIC ---

    @GetMapping("/bookreservation")
    public String getBookingReservationPage(Model model) {
        model.addAttribute("bookingRequest", new BookingRequest());
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        return "main/bookreservation";
    }

    @PostMapping("/process-booking")
    public String processBooking(@ModelAttribute("bookingRequest") BookingRequest request,
                                 RedirectAttributes redirectAttributes) {

        LocalDate checkIn = LocalDate.parse(request.getCheckInDate(), formatter);
        LocalDate checkOut = LocalDate.parse(request.getCheckOutDate(), formatter);

        // Validation
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId()).orElse(null);
        if (roomType == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid Room Selected");
            return "redirect:/bookreservation";
        }

        // Guest Logic
        Guest guest = guestRepository.findByEmail(request.getGuestEmail());
        if (guest == null) { guest = new Guest(); }
        guest.setFirstName(request.getGuestFirstName());
        guest.setLastName(request.getGuestLastName());
        guest.setEmail(request.getGuestEmail());
        guest.setPhone(request.getGuestPhone());
        guestRepository.save(guest);

        // Availability Logic
        Room availableRoom = bookingService.findAvailableRoom(request.getRoomTypeId(), checkIn, checkOut);
        if (availableRoom == null) {
            redirectAttributes.addFlashAttribute("error", "Sorry! No " + roomType.getName() + " rooms are available.");
            return "redirect:/bookreservation";
        }

        // Price Logic
        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (numberOfNights < 1) numberOfNights = 1;
        BigDecimal totalPrice = roomType.getPricePerNight().multiply(new BigDecimal(numberOfNights));

        // Save Booking
        Booking booking = new Booking();
        booking.setGuest(guest);
        booking.setRoom(availableRoom);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumGuests(request.getNumGuests());
        booking.setTotalPrice(totalPrice);
        booking.setStatus("Pending");
        booking.setBookingDate(LocalDateTime.now());
        booking.setMessage(request.getMessage());
        bookingRepository.save(booking);

        // --- FIX 3: Update the PHYSICAL ROOM status in SQL ---
        // This ensures the room_tbl actually changes from 'Available' to 'Occupied' (or 'Pending')
        availableRoom.setStatus("Occupied");
        roomRepository.save(availableRoom);
        // ----------------------------------------------------

        // Save Payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("Pending");
        paymentRepository.save(payment);

        return "redirect:/booking-success/" + booking.getBookingId();
    }

    @GetMapping("/booking-success/{id}")
    public String getBookingSuccessPage(@PathVariable("id") Integer id, Model model) {
        Optional<Booking> bookingOptional = bookingRepository.findById(id);

        if (bookingOptional.isEmpty()) {
            return "redirect:/index";
        }

        Booking booking = bookingOptional.get();
        Payment payment = paymentRepository.findByBooking(booking);

        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);

        return "main/booking-success";
    }

    // --- ADMIN PANEL ---
    // (Keep the rest of your Admin code exactly the same as before)
    @GetMapping("/login") public String getLoginPage() { return "admin/login"; }

    @GetMapping("/dashboard")
    public String getDashboardPage(Model model) {
        long totalBookings = bookingRepository.count();
        model.addAttribute("totalBookings", totalBookings);
        long totalRoomsInService = roomRepository.countByStatus("Available");
        long occupiedToday = bookingRepository.countOccupiedRoomsForToday(LocalDate.now());
        long availableRoomsNow = totalRoomsInService - occupiedToday;
        model.addAttribute("availableRooms", availableRoomsNow);
        long totalGuests = guestRepository.count();
        model.addAttribute("totalGuests", totalGuests);
        BigDecimal totalRevenue = paymentRepository.findTotalRevenue();
        model.addAttribute("totalRevenue", (totalRevenue != null) ? totalRevenue : BigDecimal.ZERO);
        return "admin/dashboard";
    }

    @GetMapping("/register") public String getRegisterPage() { return "admin/register"; }
    @GetMapping("/forgot-password") public String getForgotPasswordPage() { return "admin/forgot-password"; }
    @GetMapping("/add-room") public String getAddRoomPage(Model model) {
        model.addAttribute("roomType", new RoomType());
        model.addAttribute("pageTitle", "Add Room Type");
        return "admin/add-room";
    }
    @GetMapping("/all-rooms") public String getAllRoomsPage(Model model) {
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        return "admin/all-rooms";
    }
    @GetMapping("/edit-room/{id}") public String getEditRoomPage(@PathVariable("id") Integer id, Model model) {
        Optional<RoomType> rt = roomTypeRepository.findById(id);
        if (rt.isPresent()) {
            model.addAttribute("roomType", rt.get());
            model.addAttribute("pageTitle", "Edit Room Type");
            return "admin/add-room";
        }
        return "redirect:/all-rooms";
    }
    @GetMapping("/physical-rooms") public String getAllPhysicalRoomsPage(Model model) {
        model.addAttribute("rooms", roomRepository.findAllWithRoomType());
        return "admin/all-physical-rooms";
    }
    @GetMapping("/add-physical-room") public String getAddPhysicalRoomPage(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        model.addAttribute("pageTitle", "Add Physical Room");
        return "admin/add-physical-room";
    }
    @GetMapping("/edit-physical-room/{id}") public String getEditPhysicalRoomPage(@PathVariable("id") Integer id, Model model) {
        Optional<Room> r = roomRepository.findById(id);
        if (r.isPresent()) {
            model.addAttribute("room", r.get());
            model.addAttribute("roomTypes", roomTypeRepository.findAll());
            model.addAttribute("pageTitle", "Edit Physical Room");
            return "admin/add-physical-room";
        }
        return "redirect:/physical-rooms";
    }
    @GetMapping("/all-customer") public String getAllCustomerPage(Model model) {
        model.addAttribute("guests", guestRepository.findAll());
        return "admin/all-customer";
    }
    @GetMapping("/add-customer") public String getAddCustomerPage(Model model) {
        model.addAttribute("guest", new Guest());
        model.addAttribute("pageTitle", "Add Customer");
        return "admin/add-customer";
    }
    @GetMapping("/edit-customer/{id}") public String getEditCustomerPage(@PathVariable("id") Integer id, Model model) {
        Optional<Guest> g = guestRepository.findById(id);
        if (g.isPresent()) {
            model.addAttribute("guest", g.get());
            model.addAttribute("pageTitle", "Edit Customer");
            return "admin/add-customer";
        }
        return "redirect:/all-customer";
    }
    @GetMapping("/all-booking") public String getAllBookingPage(Model model) {
        model.addAttribute("bookings", bookingRepository.findAllWithDetails());
        return "admin/all-booking";
    }
    @GetMapping("/add-booking") public String getAddBookingPage(Model model) {
        model.addAttribute("bookingRequest", new BookingRequest());
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        model.addAttribute("pageTitle", "Add Booking");
        model.addAttribute("bookingId", null);
        return "admin/add-booking";
    }
    @GetMapping("edit-booking/{id}") public String getEditBookingPage(@PathVariable("id") Integer id, Model model) {
        Optional<Booking> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isEmpty()) return "redirect:/all-booking";
        Booking booking = bookingOptional.get();
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setGuestFirstName(booking.getGuest().getFirstName());
        bookingRequest.setGuestLastName(booking.getGuest().getLastName());
        bookingRequest.setGuestEmail(booking.getGuest().getEmail());
        bookingRequest.setGuestPhone(booking.getGuest().getPhone());
        bookingRequest.setRoomTypeId(booking.getRoom().getRoomType().getRoomTypeId());
        bookingRequest.setNumGuests(booking.getNumGuests());
        bookingRequest.setMessage(booking.getMessage());
        bookingRequest.setCheckInDate(booking.getCheckInDate().format(adminFormatter));
        bookingRequest.setCheckOutDate(booking.getCheckOutDate().format(adminFormatter));
        Payment payment = paymentRepository.findByBooking(booking);
        if (payment != null) {
            bookingRequest.setPaymentMethod(payment.getPaymentMethod());
            bookingRequest.setPaymentStatus(payment.getStatus());
        } else {
            bookingRequest.setPaymentStatus("Pending");
        }
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("bookingId", booking.getBookingId());
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        model.addAttribute("pageTitle", "Edit Booking");
        return "admin/add-booking";
    }
    @GetMapping("payments") public String getPaymentsPage(Model model) {
        model.addAttribute("payments", paymentRepository.findAllWithBookingAndGuest());
        return "admin/payments";
    }
    @GetMapping("profile") public String getProfilePage() { return "admin/profile"; }
    @GetMapping("/add-pricing") public String getAddPricingPage() { return "admin/add-pricing"; }
    @GetMapping("change-password") public String getChangePasswordPage() { return "admin/change-password"; }
    @GetMapping("edit-pricing") public String getEditPricingPage() { return "admin/edit-pricing"; }
    @GetMapping("edit-profile") public String getEditProfilePage() { return "admin/edit-profile"; }
    @GetMapping("employees") public String getEmployeesPage() { return "admin/employees"; }
    @GetMapping("lock-screen") public String getLockScreenPage() { return "admin/lock-screen"; }
    @GetMapping("pricing") public String getPricingPage() { return "admin/pricing"; }
}