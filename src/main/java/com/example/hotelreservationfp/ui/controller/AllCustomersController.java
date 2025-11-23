package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.Guest;
import com.example.hotelreservationfp.repository.GuestRepository;
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
public class AllCustomersController {

    @Autowired private GuestRepository guestRepository;
    @Autowired private ApplicationContext context;

    @FXML private TableView<Guest> customersTable;
    @FXML private TableColumn<Guest, Integer> idColumn;
    @FXML private TableColumn<Guest, String> nameColumn;
    @FXML private TableColumn<Guest, String> emailColumn;
    @FXML private TableColumn<Guest, String> phoneColumn;
    @FXML private TableColumn<Guest, Void> actionsColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFirstName() + " " + cell.getValue().getLastName()));

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
        customersTable.setItems(FXCollections.observableArrayList(guestRepository.findAll()));
    }

    @FXML
    public void showAddCustomerForm() {
        navigate(null);
    }

    private void handleEdit(Guest guest) {
        navigate(guest.getGuestId());
    }

    private void handleDelete(Guest guest) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete guest " + guest.getFirstName() + "?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                guestRepository.delete(guest);
                loadData();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Cannot delete. Guest likely has active bookings.").show();
            }
        }
    }

    private void navigate(Integer id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-customer.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();

            if(id != null) {
                AddCustomerController controller = loader.getController();
                controller.setGuestId(id);
            }

            StackPane contentArea = (StackPane) customersTable.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}