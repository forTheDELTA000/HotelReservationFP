package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.RoomType;
import com.example.hotelreservationfp.repository.RoomTypeRepository;
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
public class AllRoomTypesController {

    @Autowired private RoomTypeRepository roomTypeRepository;
    @Autowired private ApplicationContext context;

    @FXML private TableView<RoomType> roomTypesTable;
    @FXML private TableColumn<RoomType, String> nameColumn;
    @FXML private TableColumn<RoomType, Double> priceColumn;
    @FXML private TableColumn<RoomType, Integer> guestsColumn;
    @FXML private TableColumn<RoomType, String> descriptionColumn;
    @FXML private TableColumn<RoomType, Void> actionsColumn;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        guestsColumn.setCellValueFactory(new PropertyValueFactory<>("maxGuests"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

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
        roomTypesTable.setItems(FXCollections.observableArrayList(roomTypeRepository.findAll()));
    }

    @FXML
    public void showAddRoomTypeForm() {
        navigate(null);
    }

    private void handleEdit(RoomType type) {
        navigate(type.getRoomTypeId());
    }

    private void handleDelete(RoomType type) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Room Type " + type.getName() + "?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                roomTypeRepository.delete(type);
                loadData();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Cannot delete. This Room Type is used by physical rooms.").show();
            }
        }
    }

    private void navigate(Integer id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-room-type.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();

            if (id != null) {
                // Make sure AddRoomTypeController has a setRoomTypeId(id) method!
                AddRoomTypeController controller = loader.getController();
                controller.setRoomTypeId(id);
            }

            StackPane contentArea = (StackPane) roomTypesTable.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}