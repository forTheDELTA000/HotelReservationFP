package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.JavaFxApplication;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class LockScreenController {
    @FXML private Label userNameLabel;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML public void handleUnlock() {
        // Simplified unlock logic
        if (!passwordField.getText().isEmpty()) {
            JavaFxApplication.switchScene("/fxml/main_layout.fxml", "Dashboard", (Stage) passwordField.getScene().getWindow());
        } else {
            messageLabel.setText("Invalid Password");
            messageLabel.setVisible(true);
        }
    }
    @FXML public void handleLoginLink() {
        JavaFxApplication.switchScene("/fxml/login.fxml", "Login", (Stage) passwordField.getScene().getWindow());
    }
}