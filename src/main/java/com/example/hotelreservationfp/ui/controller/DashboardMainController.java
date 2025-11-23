package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.repository.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DashboardMainController {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private GuestRepository guestRepository;
    @Autowired private PaymentRepository paymentRepository;

    @FXML private Label totalBookingsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label totalGuestsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private VBox lineChartContainer;
    @FXML private VBox donutChartContainer;

    @FXML
    public void initialize() {
        // 1. Load Stats
        totalBookingsLabel.setText(String.valueOf(bookingRepository.count()));
        totalGuestsLabel.setText(String.valueOf(guestRepository.count()));

        // Logic: Total Rooms - Occupied Today
        long totalRooms = roomRepository.count();
        long occupied = bookingRepository.countOccupiedRoomsForToday(LocalDate.now());
        availableRoomsLabel.setText(String.valueOf(totalRooms - occupied));

        BigDecimal revenue = paymentRepository.findTotalRevenue();
        totalRevenueLabel.setText(revenue != null ? "$" + revenue : "$0.00");
    }
}