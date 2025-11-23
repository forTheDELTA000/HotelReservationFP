package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.*;
import com.example.hotelreservationfp.repository.*;
import jakarta.persistence.EntityManager;      // <--- NEW IMPORT
import jakarta.persistence.PersistenceContext; // <--- NEW IMPORT
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

    // --- NEW: Inject EntityManager to handle manual session clearing ---
    @PersistenceContext
    private EntityManager entityManager;

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

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5, editBtn, deleteBtn);

            {
                // Updated Style to match your new CSS classes if you prefer, or keep inline
                editBtn.getStyleClass().add("btn-primary"); // Or keep inline style
                deleteBtn.getStyleClass().add("btn-secondary"); // Or keep inline style
                editBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
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

    @FXML public void showAddBookingForm() { navigate(null); }

    private void handleEdit(Booking b) { navigate(b.getBookingId()); }

    // --- REWRITTEN DELETE METHOD ---
    private void handleDelete(Booking b) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Booking?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {

            try {
                // 1. Get the IDs needed for deletion
                Integer bookingId = b.getBookingId();
                Integer paymentId = null;

                // 2. Fetch a fresh copy from DB to check current status
                Booking freshBooking = bookingRepository.findById(bookingId).orElse(null);

                if (freshBooking != null) {
                    // Fix Room Status first (This is safe)
                    if (freshBooking.getRoom() != null) {
                        freshBooking.getRoom().setStatus("Available");
                        roomRepository.save(freshBooking.getRoom());
                    }

                    // Check if a payment exists and get its ID
                    Payment p = paymentRepository.findByBooking(freshBooking);
                    if (p != null) {
                        paymentId = p.getPaymentId();
                    }
                }

                // 3. THE FIX: Clear the Hibernate Session
                // This forces Hibernate to "forget" the loaded objects.
                // It stops Hibernate from checking if the Booking in memory references the Payment.
                entityManager.clear();

                // 4. Delete Payment by ID directly (if it existed)
                if (paymentId != null) {
                    paymentRepository.deleteById(paymentId);
                }

                // 5. Delete Booking by ID directly
                bookingRepository.deleteById(bookingId);

                // 6. Refresh UI
                loadBookings();

            } catch (Exception e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Could not delete booking: " + e.getMessage());
                errorAlert.show();
            }
        }
    }

    private void navigate(Integer id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-booking.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();
            if (id != null) ((AddBookingController) loader.getController()).setBookingId(id);

            StackPane contentArea = (StackPane) bookingsTable.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}