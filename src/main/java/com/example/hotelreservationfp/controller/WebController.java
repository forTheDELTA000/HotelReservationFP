package com.example.hotelreservationfp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
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

    @GetMapping("/login")
    public String getLoginPage() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String getDashboardPage() {
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

    @GetMapping("/add-booking")
    public String getAddBookingPage() {
        return "admin/add-booking";
    }

    @GetMapping("/add-customer")
    public String getAddCustomerPage() {
        return "admin/add-customer";
    }

    @GetMapping("/add-employee")
    public String getAddEmployeePage() {
        return "admin/add-employee";
    }

    @GetMapping("/add-pricing")
    public String getAddPricingPage() {
        return "admin/add-pricing";
    }

    @GetMapping("/add-room")
    public String getAddRoomPage() {
        return "admin/add-room";
    }

    @GetMapping("/add-staff")
    public String getAddStaffPage() {
        return "admin/add-staff";
    }

    @GetMapping("/all-booking")
    public String getAllBookingPage() {
        return "admin/all-booking";
    }

    @GetMapping("/all-rooms")
    public String getAllRoomsPage() {
        return "admin/all-rooms";
    }

    @GetMapping("/all-customer")
    public String getAllCustomerPage() {
        return "admin/all-customer";
    }

    @GetMapping("/all-staff")
    public String getAllStaffPage() {
        return "admin/all-staff";
    }

    @GetMapping("change-password")
    public String getChangePasswordPage() {
        return "admin/change-password";
    }

    @GetMapping("edit-booking")
    public String getEditBookingPage() {
        return "admin/edit-booking";
    }

    @GetMapping("edit-customer")
    public String getCustomerPage() {
        return "admin/edit-customer";
    }

    @GetMapping("edit-employee")
    public String getEditEmployeePage() {
        return "admin/edit-employee";
    }

    @GetMapping("edit-pricing")
    public String getEditPricingPage() {
        return "admin/edit-pricing";
    }

    @GetMapping("edit-profile")
    public String getEditProfilePage() {
        return "admin/edit-profile";
    }

    @GetMapping("edit-room")
    public String getEditRoomPage() {
        return "admin/edit-room";
    }

    @GetMapping("edit-staff")
    public String getEditStaffPage() {
        return "admin/edit-staff";
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
    public String getPaymentsPage() {
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
