package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.Guest;
import com.example.hotelreservationfp.repository.GuestRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class AddCustomerController {

    @Autowired private GuestRepository guestRepository;
    @Autowired private ApplicationContext context;

    @FXML private Label pageTitle, breadcrumbActive;
    @FXML private TextField guestIdField, firstNameField, lastNameField, emailField, phoneField;

    // FIX: Added this method
    public void setGuestId(Integer id) {
        if (id != null) {
            guestIdField.setText(String.valueOf(id));
            pageTitle.setText("Edit Customer");
            breadcrumbActive.setText("Edit Customer");

            Guest g = guestRepository.findById(id).orElse(null);
            if(g != null) {
                firstNameField.setText(g.getFirstName());
                lastNameField.setText(g.getLastName());
                emailField.setText(g.getEmail());
                phoneField.setText(g.getPhone());
            }
        }
    }

    @FXML
    public void handleSaveCustomer() {
        Guest g;
        if (guestIdField.getText() != null && !guestIdField.getText().isEmpty()) {
            int id = Integer.parseInt(guestIdField.getText());
            g = guestRepository.findById(id).orElse(new Guest());
        } else {
            g = new Guest();
        }

        g.setFirstName(firstNameField.getText());
        g.setLastName(lastNameField.getText());
        g.setEmail(emailField.getText());
        g.setPhone(phoneField.getText());

        guestRepository.save(g);
        handleCancel();
    }

    @FXML
    public void handleCancel() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/all-customer-list.fxml"), null, null, context::getBean);
            StackPane contentArea = (StackPane) firstNameField.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(view);
        } catch (IOException e) { e.printStackTrace(); }
    }
}