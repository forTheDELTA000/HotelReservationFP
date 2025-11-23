package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.JavaFxApplication;
import com.example.hotelreservationfp.entity.AdminUser;
import com.example.hotelreservationfp.repository.AdminUserRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class ForgotPasswordController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private ApplicationContext context;

    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    @FXML
    public void handleResetPassword() {
        String email = emailField.getText();

        if (email == null || email.trim().isEmpty()) {
            showError("Please enter your email.");
            return;
        }

        // 1. Verify email exists in DB
        AdminUser user = adminUserRepository.findByEmail(email);

        if (user != null) {
            // Email found, proceed to reset screen
            navigateToChangePassword(user.getEmail());
        } else {
            showError("Email does not exist in our records.");
        }
    }

    private void navigateToChangePassword(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/change-password.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            // 2. Pass the email to the next controller so it knows who to update
            ChangePasswordController controller = loader.getController();
            controller.setTargetEmail(email);

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("Reset Password");

            Scene scene = new Scene(root);

            // 3. FIX: Re-attach CSS so the next screen isn't unstyled
            URL cssResource = getClass().getResource("/static/admin/assets/css/style.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("System Error: Could not load reset screen.");
        }
    }

    @FXML
    public void handleLoginLink() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        JavaFxApplication.switchScene("/fxml/login.fxml", "Login", stage);
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("alert-error", "alert-success");
        messageLabel.getStyleClass().add("alert-error");
        messageLabel.setVisible(true);
    }
}