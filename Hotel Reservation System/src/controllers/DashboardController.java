package controllers;

import database.HotelDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import main.SceneManager;
import models.Guest;
import models.Invoice;
import models.Reservation;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private Label prefLabel;
    @FXML private ListView<String> reservationsList;
    @FXML private Label messageLabel;

    private Guest guest;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        guest = SceneManager.getCurrentGuest();

        if (guest != null) {
            welcomeLabel.setText("Welcome, " + guest.getUsername() + "!");
            balanceLabel.setText("Balance: $" + guest.getBalance());
            prefLabel.setText("Room Preference: " + guest.getRoomPreferences());
            loadReservations();
        }
    }

    private void loadReservations() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuest().getUsername().equals(guest.getUsername())) {
                boolean isPaid = false;
                for (Invoice inv : HotelDatabase.invoices) {
                    if (inv.getReservation() == r && inv.isPaid()) {
                        isPaid = true;
                        break;
                    }
                }
                
                String paymentStatus = isPaid ? "Paid" : "Unpaid";
                
                items.add("Room " + r.getRoom().getRoomNumber()
                        + " | " + r.getCheckInDate()
                        + " → " + r.getCheckOutDate()
                        + " | " + r.getStatus()
                        + " | " + paymentStatus);
            }
        }
        if (items.isEmpty())
            items.add("No reservations yet.");
        reservationsList.setItems(items);
    }

    @FXML
    private void handleBrowseRooms() {
        SceneManager.switchTo("RoomBrowsing.fxml");
    }

    @FXML
    private void handleCheckout() {
        // check if there's an active unpaid reservation first
        Reservation activeRes = null;
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuest().getUsername().equals(guest.getUsername()) && r.isActive()) {
                // Check if it's already paid
                boolean isPaid = false;
                for (Invoice inv : HotelDatabase.invoices) {
                    if (inv.getReservation() == r && inv.isPaid()) {
                        isPaid = true;
                        break;
                    }
                }
                
                if (!isPaid) {
                    activeRes = r;
                    break; // Found an unpaid one
                }
            }
        }

        if (activeRes == null) {
            messageLabel.setText("No active unpaid reservation found. You are all set!");
            return;
        }

        // Calculate amount for the existing active unpaid reservation
        long nights = java.time.temporal.ChronoUnit.DAYS.between(activeRes.getCheckInDate(), activeRes.getCheckOutDate());
        if (nights == 0) nights = 1; // Minimum 1 night
        double total = nights * activeRes.getRoom().getPricePerNight();

        // Pass to SceneManager so CheckoutController knows what it's paying for
        SceneManager.setPendingReservation(activeRes);
        SceneManager.setPendingAmount(total);

        SceneManager.switchTo("Checkout.fxml");
    }

    @FXML
    private void handleCancel() {
        // find active reservation
        Reservation toCancel = null;
        for (Reservation r : HotelDatabase.reservations)
            if (r.getGuest().getUsername().equals(guest.getUsername()) && r.isActive())
                toCancel = r;

        if (toCancel == null) {
            messageLabel.setText("No active reservation to cancel.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Reservation");
        confirm.setHeaderText(null);
        confirm.setContentText("Cancel reservation for room "
                + toCancel.getRoom().getRoomNumber() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            toCancel.cancel();
            toCancel.getRoom().setAvailable(true);
            messageLabel.setText("Reservation cancelled successfully.");
            loadReservations();
        }
    }

    @FXML
    private void handleLogout() {
        SceneManager.setCurrentGuest(null);
        SceneManager.setCurrentStaff(null);
        SceneManager.switchTo("login.fxml");
    }
}