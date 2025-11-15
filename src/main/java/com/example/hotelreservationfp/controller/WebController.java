package com.example.hotelreservationfp.controller;

import com.example.hotelreservationfp.dto.BookingRequest;
import com.example.hotelreservationfp.entity.Room;
import com.example.hotelreservationfp.entity.RoomType;
import com.example.hotelreservationfp.entity.Booking;
import com.example.hotelreservationfp.entity.Guest;
import com.example.hotelreservationfp.entity.Payment;
import com.example.hotelreservationfp.repository.PaymentRepository;
import com.example.hotelreservationfp.repository.GuestRepository;
import com.example.hotelreservationfp.repository.BookingRepository;
import com.example.hotelreservationfp.repository.RoomRepository;
import com.example.hotelreservationfp.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;
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

    // --- Main/Customer Pages ---
    @GetMapping("/")
    public String redirectToAbout() {
        return "main/index";
    }
    @GetMapping("/about-us")
    public String getAboutUsPage() {
        return "main/about-us";
    }
    @GetMapping("/contact")
    public String getContactPage() {
        return "main/contact";
    }
    @GetMapping("/index")
    public String getIndexPage() {
        return "main/index";
    }
    @GetMapping("/rooms")
    public String getRoomsPage() {
        return "main/rooms";
    }
    @GetMapping("/services")
    public String getServicesPage() {
        return "main/services";
    }
    @GetMapping("/bookreservation")
    public String getBookingPage() {
        return "main/bookreservation";
    }

    // --- Admin Login/Dashboard ---
    @GetMapping("/login")
    public String getLoginPage() {
        return "admin/login";
    }
    @GetMapping("/dashboard")
    public String getDashboardPage(Model model) {

        // 1. Get Total Bookings
        long totalBookings = bookingRepository.count();
        model.addAttribute("totalBookings", totalBookings);

        // 2. Get Available Rooms
        long availableRooms = roomRepository.countByStatus("Available");
        model.addAttribute("availableRooms", availableRooms);

        // 3. Get Total Guests (Customers)
        long totalGuests = guestRepository.count();
        model.addAttribute("totalGuests", totalGuests);

        // 4. Get Total Revenue (Collections)
        BigDecimal totalRevenue = paymentRepository.findTotalRevenue();
        model.addAttribute("totalRevenue", (totalRevenue != null) ? totalRevenue : BigDecimal.ZERO);

        // The charts (#line-chart, #donut-chart) require more complex
        // data processing, which we can add as the next feature.

        return "admin/dashboard";
    }

    @GetMapping("/register")
    public String getRegisterPage() {
        return "admin/register";
    }
    @GetMapping("/forgot-password")
    public String getForgotPasswordPage() {
        return "admin/forgot-password";
    }

    // --- Room Type Management ---
    @GetMapping("/add-room")
    public String getAddRoomPage(Model model) {
        model.addAttribute("roomType", new RoomType());
        model.addAttribute("pageTitle", "Add Room Type");
        return "admin/add-room";
    }
    @GetMapping("/all-rooms")
    public String getAllRoomsPage(Model model) {
        List<RoomType> roomTypeList = roomTypeRepository.findAll();
        model.addAttribute("roomTypes", roomTypeList);
        return "admin/all-rooms";
    }
    @GetMapping("/edit-room/{id}")
    public String getEditRoomPage(@PathVariable("id") Integer id, Model model) {
        Optional<RoomType> roomTypeOptional = roomTypeRepository.findById(id);
        if (roomTypeOptional.isPresent()) {
            model.addAttribute("roomType", roomTypeOptional.get());
            model.addAttribute("pageTitle", "Edit Room Type");
            return "admin/add-room"; // Reuses the add form
        } else {
            return "redirect:/all-rooms";
        }
    }

    // --- Physical Room Management ---
    @GetMapping("/physical-rooms")
    public String getAllPhysicalRoomsPage(Model model) {
        List<Room> roomList = roomRepository.findAllWithRoomType();
        model.addAttribute("rooms", roomList);
        return "admin/all-physical-rooms";
    }
    @GetMapping("/add-physical-room")
    public String getAddPhysicalRoomPage(Model model) {
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("pageTitle", "Add Physical Room");
        return "admin/add-physical-room";
    }
    @GetMapping("/edit-physical-room/{id}")
    public String getEditPhysicalRoomPage(@PathVariable("id") Integer id, Model model) {
        Optional<Room> roomOptional = roomRepository.findById(id);
        if (roomOptional.isPresent()) {
            List<RoomType> roomTypes = roomTypeRepository.findAll();
            model.addAttribute("room", roomOptional.get());
            model.addAttribute("roomTypes", roomTypes);
            model.addAttribute("pageTitle", "Edit Physical Room");
            return "admin/add-physical-room"; // We reuse the same form
        } else {
            return "redirect:/physical-rooms";
        }
    }

    // --- Customer (Guest) Management ---
    @GetMapping("/all-customer")
    public String getAllCustomerPage(Model model) {
        List<Guest> guestList = guestRepository.findAll();
        model.addAttribute("guests", guestList);
        return "admin/all-customer";
    }
    @GetMapping("/add-customer")
    public String getAddCustomerPage(Model model) {
        model.addAttribute("guest", new Guest());
        model.addAttribute("pageTitle", "Add Customer");
        return "admin/add-customer";
    }
    @GetMapping("/edit-customer/{id}")
    public String getEditCustomerPage(@PathVariable("id") Integer id, Model model) {
        Optional<Guest> guestOptional = guestRepository.findById(id);
        if (guestOptional.isPresent()) {
            model.addAttribute("guest", guestOptional.get());
            model.addAttribute("pageTitle", "Edit Customer");
            return "admin/add-customer"; // We reuse the "add" form
        } else {
            return "redirect:/all-customer";
        }
    }

    // --- Booking Management (THE CORRECT, FINAL VERSIONS) ---
    @GetMapping("/all-booking")
    public String getAllBookingPage(Model model) {
        List<Booking> bookingList = bookingRepository.findAllWithDetails();
        model.addAttribute("bookings", bookingList);
        return "admin/all-booking";
    }

    @GetMapping("/add-booking")
    public String getAddBookingPage(Model model) {
        model.addAttribute("bookingRequest", new BookingRequest());
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        model.addAttribute("pageTitle", "Add Booking"); // Added page title
        return "admin/add-booking";
    }

    @GetMapping("edit-booking/{id}")
    public String getEditBookingPage(@PathVariable("id") Integer id, Model model) {
        Optional<Booking> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isEmpty()) {
            return "redirect:/all-booking";
        }

        Booking booking = bookingOptional.get();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        BookingRequest bookingRequest = new BookingRequest();

        // --- Populate DTO with Guest and Booking info ---
        bookingRequest.setGuestFirstName(booking.getGuest().getFirstName());
        bookingRequest.setGuestLastName(booking.getGuest().getLastName());
        bookingRequest.setGuestEmail(booking.getGuest().getEmail());
        bookingRequest.setGuestPhone(booking.getGuest().getPhone());
        bookingRequest.setRoomTypeId(booking.getRoom().getRoomType().getRoomTypeId());
        bookingRequest.setNumGuests(booking.getNumGuests());
        bookingRequest.setCheckInDate(booking.getCheckInDate().format(formatter));
        bookingRequest.setCheckOutDate(booking.getCheckOutDate().format(formatter));

        // --- Populate DTO with Payment info ---
        Payment payment = paymentRepository.findByBooking(booking);
        if (payment != null) {
            bookingRequest.setPaymentMethod(payment.getPaymentMethod());
            bookingRequest.setPaymentStatus(payment.getStatus());
        } else {
            bookingRequest.setPaymentStatus("Pending"); // Default
        }

        // --- Send data to the page ---
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("bookingId", booking.getBookingId());
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        model.addAttribute("pageTitle", "Edit Booking");

        return "admin/add-booking"; // Reuse the add-booking form
    }

    // --- Other Admin Pages ---

    @GetMapping("/add-pricing")
    public String getAddPricingPage() {
        return "admin/add-pricing";
    }
    @GetMapping("change-password")
    public String getChangePasswordPage() {
        return "admin/change-password";
    }
    @GetMapping("edit-pricing")
    public String getEditPricingPage() {
        return "admin/edit-pricing";
    }
    @GetMapping("edit-profile")
    public String getEditProfilePage() {
        return "admin/edit-profile";
    }
    @GetMapping("employees")
    public String getEmployeesPage() {
        return "admin/employees";
    }
    @GetMapping("lock-screen")
    public String getLockScreenPage() {
        return "admin/lock-screen";
    }
    @GetMapping("payments")
    public String getPaymentsPage(Model model) { // Added Model

        // 1. Call our new repository method
        List<Payment> paymentList = paymentRepository.findAllWithBookingAndGuest();

        // 2. Send the list of payments to the HTML page
        model.addAttribute("payments", paymentList);

        return "admin/payments";
    }

    @GetMapping("pricing")
    public String getPricingPage() {
        return "admin/pricing";
    }
    @GetMapping("profile")
    public String getProfilePage() {
        return "admin/profile";
    }
}