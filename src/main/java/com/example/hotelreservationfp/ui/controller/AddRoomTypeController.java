package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.RoomType;
import com.example.hotelreservationfp.repository.RoomTypeRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.math.BigDecimal;

@Component
public class AddRoomTypeController {

    @Autowired private RoomTypeRepository roomTypeRepository;
    @Autowired private ApplicationContext context;

    @FXML private Label pageTitle, breadcrumbActive;
    @FXML private TextField roomTypeIdField, nameField, pricePerNightField, imageUrlField;
    @FXML private ComboBox<Integer> maxGuestsCombo;
    @FXML private TextArea descriptionArea;

    @FXML
    public void initialize() {
        maxGuestsCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));
    }

    // FIX: Added this method to handle Edit Mode
    public void setRoomTypeId(Integer id) {
        if (id != null) {
            roomTypeIdField.setText(String.valueOf(id));
            pageTitle.setText("Edit Room Type");
            breadcrumbActive.setText("Edit Room Type");

            RoomType rt = roomTypeRepository.findById(id).orElse(null);
            if (rt != null) {
                nameField.setText(rt.getName());
                pricePerNightField.setText(String.valueOf(rt.getPricePerNight()));
                maxGuestsCombo.setValue(rt.getMaxGuests());
                imageUrlField.setText(rt.getImageUrl());
                descriptionArea.setText(rt.getDescription());
            }
        }
    }

    @FXML
    public void handleSaveRoomType() {
        RoomType rt;
        // Check if we are editing (ID exists) or creating new
        if (roomTypeIdField.getText() != null && !roomTypeIdField.getText().isEmpty()) {
            int id = Integer.parseInt(roomTypeIdField.getText());
            rt = roomTypeRepository.findById(id).orElse(new RoomType());
        } else {
            rt = new RoomType();
        }

        rt.setName(nameField.getText());
        // Basic error handling for number format
        try {
            rt.setPricePerNight(new BigDecimal(pricePerNightField.getText()));
        } catch (Exception e) {
            rt.setPricePerNight(BigDecimal.ZERO);
        }
        rt.setMaxGuests(maxGuestsCombo.getValue());
        rt.setImageUrl(imageUrlField.getText());
        rt.setDescription(descriptionArea.getText());

        roomTypeRepository.save(rt);
        handleCancel();
    }

    @FXML
    public void handleCancel() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/all-room-types.fxml"), null, null, context::getBean);
            // Find the main content area (StackPane) and replace the view
            StackPane contentArea = (StackPane) nameField.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}