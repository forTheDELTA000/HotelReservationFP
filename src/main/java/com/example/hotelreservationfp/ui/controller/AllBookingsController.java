package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.*;
import com.example.hotelreservationfp.repository.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class AllBookingsController {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private ApplicationContext context;

    @FXML private TextField searchField;
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> idColumn;
    @FXML private TableColumn<Booking, String> guestNameColumn;
    @FXML private TableColumn<Booking, String> roomTypeColumn;
    @FXML private TableColumn<Booking, String> checkInColumn;
    @FXML private TableColumn<Booking, String> checkOutColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, Void> actionsColumn;

    @FXML
    public void initialize() {
        setupColumns();
        loadBookings();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        checkInColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckInDate().toString()));
        checkOutColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckOutDate().toString()));

        guestNameColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getGuest().getFirstName() + " " + c.getValue().getGuest().getLastName()));

        roomTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRoom().getRoomType().getName()));

        // Add Edit/Delete Buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5, editBtn, deleteBtn);

            {
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                editBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML public void loadBookings() {
        bookingsTable.setItems(FXCollections.observableArrayList(bookingRepository.findAllWithDetails()));
    }

    @FXML public void handleSearch() { /* Implement search filter logic */ }

    @FXML public void showAddBookingForm() { navigate(null); }

    private void handleEdit(Booking b) { navigate(b.getBookingId()); }

    private void handleDelete(Booking b) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Booking?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            Payment p = paymentRepository.findByBooking(b);
            if (p != null) { b.setPayment(null); paymentRepository.delete(p); }
            Room r = b.getRoom();
            if (r != null) { r.setStatus("Available"); roomRepository.save(r); }
            bookingRepository.delete(b);
            loadBookings();
        }
    }

    private void navigate(Integer id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-booking.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();
            if (id != null) ((AddBookingController) loader.getController()).setBookingId(id);

            // Find the StackPane in MainLayout (assumes MainLayout id is contentArea)
            StackPane contentArea = (StackPane) bookingsTable.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(view);
        } catch (IOException e) { e.printStackTrace(); }
    }
}