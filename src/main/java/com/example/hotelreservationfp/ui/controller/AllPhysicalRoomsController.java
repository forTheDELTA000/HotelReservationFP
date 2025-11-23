package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.Room;
import com.example.hotelreservationfp.repository.RoomRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class AllPhysicalRoomsController {

    @Autowired private RoomRepository roomRepository;
    @Autowired private ApplicationContext context;

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> roomNumberColumn;
    @FXML private TableColumn<Room, String> roomTypeColumn;
    @FXML private TableColumn<Room, String> statusColumn;
    @FXML private TableColumn<Room, Void> actionsColumn; // Use Void for button columns

    @FXML
    public void initialize() {
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Show Room Type Name
        roomTypeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRoomType().getName()));

        // --- FIX: ADD BUTTONS ---
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 10px;");
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");

                editBtn.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        loadData();
    }

    private void loadData() {
        roomsTable.setItems(FXCollections.observableArrayList(roomRepository.findAll()));
    }

    @FXML
    public void showAddPhysicalRoomForm() {
        navigate(null);
    }

    private void handleEdit(Room room) {
        navigate(room.getRoomId());
    }

    private void handleDelete(Room room) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Room " + room.getRoomNumber() + "?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                roomRepository.delete(room);
                loadData();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Cannot delete. This room might be linked to existing bookings.").show();
            }
        }
    }

    private void navigate(Integer id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-physical-room.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();

            if (id != null) {
                // Pass the ID to the form controller for editing
                AddPhysicalRoomController controller = loader.getController();
                controller.setRoomId(id);
            }

            StackPane contentArea = (StackPane) roomsTable.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}