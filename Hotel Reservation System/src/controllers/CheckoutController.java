package controllers;

import enums.paymentmethod;
import exceptions.InvalidPaymentException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.SceneManager;
import models.Guest;
import models.Invoice;
import database.HotelDatabase;
import javafx.scene.control.Alert;

public class CheckoutController {

    @FXML
    private Label balanceLabel;
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<paymentmethod> paymentMethodCombo;
    @FXML
    private Label messageLabel;

    private Guest currentGuest;
    private double requiredAmount = 0.0;
    private models.Reservation checkoutReservation;

    @FXML
    public void initialize() {
        currentGuest = SceneManager.getCurrentGuest();
        checkoutReservation = SceneManager.getPendingReservation();
        requiredAmount = SceneManager.getPendingAmount();
        
        if (checkoutReservation != null) {
            balanceLabel.setText(String.format("Exact amount due: $%.2f", requiredAmount));
        } else {
            balanceLabel.setText("No pending checkout.");
        }
        
        // Populate the combo box with enum values
        paymentMethodCombo.setItems(FXCollections.observableArrayList(paymentmethod.values()));
    }

    @FXML
    public void handlePayment() {
        if (currentGuest == null || checkoutReservation == null) return;
        
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: red;");
        
        String amountText = amountField.getText();
        paymentmethod selectedMethod = paymentMethodCombo.getValue();
        
        if (amountText == null || amountText.trim().isEmpty()) {
            messageLabel.setText("Please enter an amount.");
            return;
        }
        
        if (selectedMethod == null) {
            messageLabel.setText("Please select a payment method.");
            return;
        }
        
        try {
            double amountToPay = Double.parseDouble(amountText);
            
            // Require exact amount
            if (amountToPay != requiredAmount) {
                messageLabel.setText(String.format("Please enter the exact amount required ($%.2f)", requiredAmount));
                return;
            }
            
            // Deduct from guest balance (affects the current balance in the program)
            currentGuest.pay(requiredAmount, selectedMethod);

            // Generate invoice and mark it paid as required by the user
            Invoice invoice = Invoice.generate(checkoutReservation, requiredAmount);
            invoice.markPaid(selectedMethod);
            HotelDatabase.invoices.add(invoice);
            
            // The room is already marked as unavailable during Guest.makeReservation().
            // If they need to "pay via guest balance", we can do it, but here it's an immediate invoice pay.
            
            // Success
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Successful");
            alert.setHeaderText(null);
            alert.setContentText("Payment successful! The room is now reserved and fully paid.");
            alert.showAndWait();
            
            // Clear checkout pending details
            SceneManager.setPendingReservation(null);
            SceneManager.setPendingAmount(0);
            
            SceneManager.switchTo("Dashboard.fxml");
            
        } catch (NumberFormatException e) {
            messageLabel.setText("Invalid amount entered. Please enter numbers only.");
        } catch (exceptions.InvalidPaymentException e) {
            messageLabel.setText("Payment failed: " + e.getMessage() + " (Please ensure you have enough balance)");
        } catch (Exception e) {
            messageLabel.setText("An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("Dashboard.fxml");
    }
}
