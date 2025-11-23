package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.*;
import com.example.hotelreservationfp.repository.*;
import com.example.hotelreservationfp.service.BookingService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class AddBookingController {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private RoomTypeRepository roomTypeRepository;
    @Autowired private GuestRepository guestRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private BookingService bookingService;
    @Autowired private ApplicationContext context;

    @FXML private Label formTitle, errorMessageLabel;
    @FXML private TextField guestFirstNameField, guestLastNameField, guestEmailField, guestPhoneField;
    @FXML private ComboBox<RoomType> roomTypeCombo;
    @FXML private ComboBox<Integer> numGuestsCombo;
    @FXML private DatePicker checkInDateField, checkOutDateField;
    @FXML private ComboBox<String> paymentMethodCombo, paymentStatusCombo;
    @FXML private TextArea messageArea;
    @FXML private Button saveButton;

    private Integer bookingId;

    @FXML
    public void initialize() {
        roomTypeCombo.setItems(FXCollections.observableArrayList(roomTypeRepository.findAll()));
        roomTypeCombo.setConverter(new StringConverter<>() {
            @Override public String toString(RoomType r) { return r == null ? "" : r.getName(); }
            @Override public RoomType fromString(String s) { return null; }
        });
        numGuestsCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        paymentMethodCombo.setItems(FXCollections.observableArrayList("Cash", "Credit Card"));
        paymentStatusCombo.setItems(FXCollections.observableArrayList("Pending", "Paid"));
    }

    public void setBookingId(Integer id) {
        this.bookingId = id;
        if(id != null) {
            formTitle.setText("Edit Booking");
            Booking b = bookingRepository.findById(id).orElse(null);
            if(b != null) {
                guestFirstNameField.setText(b.getGuest().getFirstName());
                guestLastNameField.setText(b.getGuest().getLastName());
                guestEmailField.setText(b.getGuest().getEmail());
                guestPhoneField.setText(b.getGuest().getPhone());
                roomTypeCombo.setValue(b.getRoom().getRoomType());
                numGuestsCombo.setValue(b.getNumGuests());
                checkInDateField.setValue(b.getCheckInDate());
                checkOutDateField.setValue(b.getCheckOutDate());
                Payment p = paymentRepository.findByBooking(b);
                if(p!=null) {
                    paymentMethodCombo.setValue(p.getPaymentMethod());
                    paymentStatusCombo.setValue(p.getStatus());
                }
            }
        }
    }

    @FXML
    public void handleSaveBooking() {
        try {
            // Guest Logic
            Guest guest = guestRepository.findByEmail(guestEmailField.getText());
            if (guest == null) guest = new Guest();
            guest.setFirstName(guestFirstNameField.getText());
            guest.setLastName(guestLastNameField.getText());
            guest.setEmail(guestEmailField.getText());
            guest.setPhone(guestPhoneField.getText());
            guestRepository.save(guest);

            Booking booking;
            Room assignedRoom;

            if (bookingId == null) {
                // New Booking
                assignedRoom = bookingService.findAvailableRoom(roomTypeCombo.getValue().getRoomTypeId(), checkInDateField.getValue(), checkOutDateField.getValue());
                if (assignedRoom == null) throw new RuntimeException("No room available.");
                booking = new Booking();
                booking.setBookingDate(LocalDateTime.now());
                // Mark Occupied
                assignedRoom.setStatus("Occupied");
                roomRepository.save(assignedRoom);
            } else {
                // Edit (Simplified logic: keep room unless full re-booking logic needed)
                booking = bookingRepository.findById(bookingId).get();
                assignedRoom = booking.getRoom();
            }

            // Calc Price
            long nights = ChronoUnit.DAYS.between(checkInDateField.getValue(), checkOutDateField.getValue());
            BigDecimal price = roomTypeCombo.getValue().getPricePerNight().multiply(BigDecimal.valueOf(nights < 1 ? 1 : nights));

            booking.setGuest(guest);
            booking.setRoom(assignedRoom);
            booking.setCheckInDate(checkInDateField.getValue());
            booking.setCheckOutDate(checkOutDateField.getValue());
            booking.setNumGuests(numGuestsCombo.getValue());
            booking.setTotalPrice(price);
            booking.setStatus("Confirmed");
            booking.setMessage(messageArea.getText());
            bookingRepository.save(booking);

            // Payment
            Payment p = paymentRepository.findByBooking(booking);
            if(p == null) { p = new Payment(); p.setBooking(booking); }
            p.setAmount(price);
            p.setPaymentMethod(paymentMethodCombo.getValue());
            p.setStatus(paymentStatusCombo.getValue());
            paymentRepository.save(p);

            handleCancel();

        } catch (Exception e) {
            errorMessageLabel.setText("Error: " + e.getMessage());
            errorMessageLabel.setVisible(true);
        }
    }

    @FXML
    public void handleCancel() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/all-booking-list.fxml"), null, null, context::getBean);
            ((StackPane) formTitle.getScene().lookup("#contentArea")).getChildren().setAll(view);
        } catch (IOException e) { e.printStackTrace(); }
    }
}