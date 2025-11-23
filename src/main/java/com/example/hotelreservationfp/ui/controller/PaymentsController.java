package com.example.hotelreservationfp.ui.controller;

import com.example.hotelreservationfp.entity.Payment;
import com.example.hotelreservationfp.repository.PaymentRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentsController {
    @Autowired private PaymentRepository paymentRepository;

    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, Integer> invoiceIdColumn;
    @FXML private TableColumn<Payment, String> customerNameColumn, paymentTypeColumn, paidDateColumn, paidAmountColumn;

    @FXML public void initialize() {
        loadPayments();
    }

    @FXML public void loadPayments() {
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paidAmountColumn.setCellValueFactory(c -> new SimpleStringProperty("$" + c.getValue().getAmount()));
        customerNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBooking().getGuest().getFirstName() + " " + c.getValue().getBooking().getGuest().getLastName()));
        paidDateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentDate() == null ? "N/A" : c.getValue().getPaymentDate().toString()));

        paymentsTable.setItems(FXCollections.observableArrayList(paymentRepository.findAllWithBookingAndGuest()));
    }
}