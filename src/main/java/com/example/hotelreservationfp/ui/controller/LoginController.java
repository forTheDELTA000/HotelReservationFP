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
public class LoginController {

    @Autowired private AdminUserRepository adminUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorMessage;
    @FXML private Label logoutMessage;

    @FXML
    public void handleLoginButtonAction() {
        String email = usernameField.getText();
        String password = passwordField.getText();

        AdminUser user = adminUserRepository.findByEmail(email);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // Success: Load the Main Layout (Sidebar + Dashboard)
            Stage stage = (Stage) usernameField.getScene().getWindow();
            JavaFxApplication.switchScene("/fxml/main_layout.fxml", "Admin Dashboard", stage);
        } else {
            errorMessage.setVisible(true);
            logoutMessage.setVisible(false);
        }
    }

    @FXML
    public void handleRegister() {
        JavaFxApplication.switchScene("/fxml/register.fxml", "Register", (Stage) usernameField.getScene().getWindow());
    }

    @FXML
    public void handleForgotPassword() {
        JavaFxApplication.switchScene("/fxml/forgot-password.fxml", "Forgot Password", (Stage) usernameField.getScene().getWindow());
    }
}