package controllers;

import database.HotelDatabase;
import enums.paymentmethod;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.SceneManager;
import models.Guest;
import models.Invoice;
import models.Reservation;

public class CheckoutController {

    @FXML private Label balanceLabel;
    @FXML private TextField amountField;
    @FXML private ComboBox<paymentmethod> paymentMethodCombo;
    @FXML private Label messageLabel;
    @FXML private Button payButton;
    @FXML private ProgressIndicator paymentIndicator;

    private Guest currentGuest;
    private double requiredAmount = 0.0;
    private Reservation checkoutReservation;

    @FXML
    public void initialize() {
        currentGuest = SceneManager.getCurrentGuest();
        checkoutReservation = SceneManager.getPendingReservation();
        requiredAmount = SceneManager.getPendingAmount();

        if (checkoutReservation != null)
            balanceLabel.setText(String.format("$%.2f", requiredAmount));
        else
            balanceLabel.setText("0.00");

        paymentMethodCombo.setItems(FXCollections.observableArrayList(paymentmethod.values()));
        if (paymentIndicator != null) paymentIndicator.setVisible(false);
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

        double amountToPay;
        try {
            amountToPay = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            messageLabel.setText("Invalid amount. Please enter numbers only.");
            return;
        }

        if (Math.abs(amountToPay - requiredAmount) > 0.001) {
            messageLabel.setText(String.format(
                    "Please enter the exact amount required ($%.2f)", requiredAmount));
            return;
        }

        if (payButton != null) payButton.setDisable(true);
        if (paymentIndicator != null) paymentIndicator.setVisible(true);
        messageLabel.setText("Processing payment...");
        messageLabel.setStyle("-fx-text-fill: gray;");

        final paymentmethod method = selectedMethod;

        Task<Void> paymentTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1500);

                // 1. Deduct balance
                currentGuest.pay(requiredAmount, method);
                HotelDatabase.updateGuestBalance(currentGuest);
                System.out.println("Balance saved: " + currentGuest.getBalance());

                // 2. Mark reservation as CONFIRMED (keep as is) and ensure room stays unavailable
                checkoutReservation.getRoom().setAvailable(false);
                HotelDatabase.updateRoomAvailability(checkoutReservation.getRoom());
                HotelDatabase.updateReservationStatus(checkoutReservation);
                System.out.println("Room availability saved: " + checkoutReservation.getRoom().isAvailable());

                // 3. Create and save invoice to DB
                Invoice invoice = Invoice.generate(checkoutReservation, requiredAmount);
                invoice.markPaid(method);
                HotelDatabase.invoices.add(invoice);
                int reservationDbId = HotelDatabase.getLastReservationId();
                HotelDatabase.saveInvoice(invoice, reservationDbId);
                System.out.println("Invoice saved for reservationId: " + reservationDbId);

                return null;
            }
        };

        paymentTask.setOnSucceeded(event -> {
            if (paymentIndicator != null) paymentIndicator.setVisible(false);
            if (payButton != null) payButton.setDisable(false);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Successful");
            alert.setHeaderText(null);
            alert.setContentText("Payment successful! The room is now reserved and fully paid.");
            alert.showAndWait();

            SceneManager.setPendingReservation(null);
            SceneManager.setPendingAmount(0);
            SceneManager.switchTo("Dashboard.fxml");
        });

        paymentTask.setOnFailed(event -> {
            if (paymentIndicator != null) paymentIndicator.setVisible(false);
            if (payButton != null) payButton.setDisable(false);
            Throwable ex = paymentTask.getException();
            messageLabel.setText("Payment failed: " + ex.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        });

        Thread thread = new Thread(paymentTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("Dashboard.fxml");
    }
}