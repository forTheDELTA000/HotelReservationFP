package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.JavaFxApplication;
import com.example.hotelreservationfp.entity.AdminUser;
import com.example.hotelreservationfp.repository.AdminUserRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RegisterController {

    @Autowired private AdminUserRepository adminUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @FXML private TextField firstNameField, lastNameField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Label errorMessageLabel;

    @FXML
    public void handleRegister() {
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match.");
            return;
        }
        if (adminUserRepository.findByEmail(emailField.getText()) != null) {
            showError("Email already exists.");
            return;
        }

        AdminUser user = new AdminUser();
        user.setFirstName(firstNameField.getText());
        user.setLastName(lastNameField.getText());
        user.setEmail(emailField.getText());
        user.setPassword(passwordEncoder.encode(passwordField.getText()));
        adminUserRepository.save(user);

        // Redirect to Login
        handleLoginLink();
    }

    @FXML
    public void handleLoginLink() {
        JavaFxApplication.switchScene("/fxml/login.fxml", "Login", (Stage) emailField.getScene().getWindow());
    }

    private void showError(String msg) {
        errorMessageLabel.setText(msg);
        errorMessageLabel.setVisible(true);
    }
}