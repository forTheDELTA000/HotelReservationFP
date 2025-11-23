package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.JavaFxApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FxMainLayoutController {

    @FXML private StackPane contentArea;
    @Autowired private ApplicationContext context;

    @FXML
    public void initialize() {
        showDashboard();
    }

    // --- Navigation Actions ---

    @FXML public void showDashboard() { loadView("/fxml/dashboard-main.fxml"); }

    @FXML public void showAllBookings() { loadView("/fxml/all-booking-list.fxml"); }
    @FXML public void showAddBooking() { loadView("/fxml/add-booking.fxml"); }

    @FXML public void showCustomers() { loadView("/fxml/all-customer-list.fxml"); }
    @FXML public void showAddCustomer() { loadView("/fxml/add-customer.fxml"); }

    @FXML public void showPhysicalRooms() { loadView("/fxml/all-physical-rooms.fxml"); }

    // ADDED THIS METHOD
    @FXML public void showAddPhysicalRoom() { loadView("/fxml/add-physical-room.fxml"); }

    @FXML public void showRoomTypes() { loadView("/fxml/all-room-types.fxml"); }

    // ADDED THIS METHOD
    @FXML public void showAddRoomType() { loadView("/fxml/add-room-type.fxml"); }

    @FXML public void showPayments() { loadView("/fxml/payments-list.fxml"); }

    @FXML
    public void handleLogout() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        JavaFxApplication.switchScene("/fxml/login.fxml", "Login", stage);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}