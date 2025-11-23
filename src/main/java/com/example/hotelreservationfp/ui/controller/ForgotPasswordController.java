package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.JavaFxApplication;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    @FXML
    public void handleResetPassword() {
        // In a real application, you would call an email service here.
        // For this desktop admin panel, we will just show a success message.
        String email = emailField.getText();

        if (email == null || email.trim().isEmpty()) {
            messageLabel.setText("Please enter your email.");
            messageLabel.setStyle("-fx-text-fill: red;");
        } else {
            messageLabel.setText("If an account exists, a reset link has been sent.");
            messageLabel.setStyle("-fx-text-fill: green;");
        }
        messageLabel.setVisible(true);
    }

    @FXML
    public void handleLoginLink() {
        // Navigate back to Login
        Stage stage = (Stage) emailField.getScene().getWindow();
        JavaFxApplication.switchScene("/fxml/login.fxml", "Login", stage);
    }
}