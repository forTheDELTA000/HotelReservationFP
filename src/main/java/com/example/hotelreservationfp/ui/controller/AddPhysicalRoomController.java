package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.Room;
import com.example.hotelreservationfp.entity.RoomType;
import com.example.hotelreservationfp.repository.RoomRepository;
import com.example.hotelreservationfp.repository.RoomTypeRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class AddPhysicalRoomController {

    @Autowired private RoomRepository roomRepository;
    @Autowired private RoomTypeRepository roomTypeRepository;
    @Autowired private ApplicationContext context;

    @FXML private Label pageTitle, breadcrumbActive;
    @FXML private TextField roomIdField, roomNumberField;
    @FXML private ComboBox<RoomType> roomTypeCombo;
    @FXML private ComboBox<String> statusCombo;

    @FXML
    public void initialize() {
        roomTypeCombo.setItems(FXCollections.observableArrayList(roomTypeRepository.findAll()));
        roomTypeCombo.setConverter(new StringConverter<>() {
            @Override public String toString(RoomType r) { return r == null ? "" : r.getName(); }
            @Override public RoomType fromString(String s) { return null; }
        });
        statusCombo.setItems(FXCollections.observableArrayList("Available", "Occupied", "Under Maintenance"));
    }

    // FIX: Added this method
    public void setRoomId(Integer id) {
        if (id != null) {
            roomIdField.setText(String.valueOf(id));
            pageTitle.setText("Edit Physical Room");
            breadcrumbActive.setText("Edit Room");

            Room r = roomRepository.findById(id).orElse(null);
            if (r != null) {
                roomNumberField.setText(r.getRoomNumber());
                roomTypeCombo.setValue(r.getRoomType());
                statusCombo.setValue(r.getStatus());
            }
        }
    }

    @FXML
    public void handleSaveRoom() {
        Room r;
        if (roomIdField.getText() != null && !roomIdField.getText().isEmpty()) {
            int id = Integer.parseInt(roomIdField.getText());
            r = roomRepository.findById(id).orElse(new Room());
        } else {
            r = new Room();
        }

        r.setRoomNumber(roomNumberField.getText());
        r.setRoomType(roomTypeCombo.getValue());
        r.setStatus(statusCombo.getValue());

        roomRepository.save(r);
        handleCancel();
    }

    @FXML
    public void handleCancel() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/all-physical-rooms.fxml"), null, null, context::getBean);
            StackPane contentArea = (StackPane) roomNumberField.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(view);
        } catch (IOException e) { e.printStackTrace(); }
    }
}