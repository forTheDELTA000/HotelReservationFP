package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.JavaFxApplication;
import com.example.hotelreservationfp.entity.AdminUser;
import com.example.hotelreservationfp.repository.AdminUserRepository;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // <--- NEW IMPORT
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private String targetEmail;

    // Create an instance of the encoder.
    // If you have a @Bean for this in your AppConfig, you can @Autowired it instead.
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Called by ForgotPasswordController to pass the verified email address.
     */
    public void setTargetEmail(String email) {
        this.targetEmail = email;
    }

    @FXML
    public void handleUpdatePassword() {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        // 1. Validation
        if (newPass == null || newPass.isEmpty() || confirmPass == null || confirmPass.isEmpty()) {
            showMessage("Please fill in all fields.", true);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showMessage("New passwords do not match.", true);
            return;
        }

        if (targetEmail == null || targetEmail.isEmpty()) {
            showMessage("Error: No user identified. Please restart the process.", true);
            return;
        }

        // 2. Fetch the user
        AdminUser user = adminUserRepository.findByEmail(targetEmail);

        if (user == null) {
            showMessage("User account not found for email: " + targetEmail, true);
            return;
        }

        // 3. Update the password WITH HASHING
        // This generates the "$2a$..." string that the Login controller expects.
        String hashedPassword = passwordEncoder.encode(newPass);
        user.setPassword(hashedPassword);

        // 4. Force immediate save to database
        try {
            adminUserRepository.saveAndFlush(user);

            // 5. Success Message & Redirect
            showMessage("Password updated successfully! Redirecting to login...", false);

            // Wait 1.5 seconds so user can read the message, then switch scenes
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(event -> {
                // Get current stage from one of the elements
                Stage stage = (Stage) newPasswordField.getScene().getWindow();
                JavaFxApplication.switchScene("/fxml/login.fxml", "Login", stage);
            });
            delay.play();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Database Error: Could not save password.", true);
        }
    }

    private void showMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        // Reset styles first
        messageLabel.getStyleClass().removeAll("alert-error", "alert-success");
        messageLabel.setStyle(""); // Clear inline styles if any

        // Apply new style
        if (isError) {
            messageLabel.getStyleClass().add("alert-error");
            messageLabel.setStyle("-fx-text-fill: red;");
        } else {
            messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
        messageLabel.setVisible(true);
    }
}