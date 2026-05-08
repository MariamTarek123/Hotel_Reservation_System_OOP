package controllers;

import database.HotelDatabase;
import enums.reservationstatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import main.SceneManager;
import models.Guest;
import models.Invoice;
import models.Reservation;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private Label prefLabel;
    @FXML private Label activeResLabel;
    @FXML private ListView<String> reservationsList;
    @FXML private Label messageLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private Guest guest;
    private ScheduledExecutorService scheduler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        guest = SceneManager.getCurrentGuest();

        if (guest != null) {
            welcomeLabel.setText("Welcome, " + guest.getUsername() + "!");
            updateUI();
            loadReservationsAsync();

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                HotelDatabase.loadAll();
                // sync guest balance from reloaded DB data
                Guest reloaded = HotelDatabase.findGuestByUsername(guest.getUsername());
                if (reloaded != null) guest.setBalance(reloaded.getBalance());
                Platform.runLater(() -> {
                    updateUI();
                    loadReservationsAsync();
                });
            }, 30, 30, TimeUnit.SECONDS);
        }
    }

    private void updateUI() {
        balanceLabel.setText("Balance: $" + guest.getBalance());
        prefLabel.setText("Room Preference: " + guest.getRoomPreferences());
        long activeCount = HotelDatabase.reservations.stream()
                .filter(r -> r.getGuest().getUsername().equals(guest.getUsername())
                        && (r.getStatus() == reservationstatus.CONFIRMED
                        || r.getStatus() == reservationstatus.PENDING))
                .count();
        if (activeResLabel != null) activeResLabel.setText(String.valueOf(activeCount));
    }

    private void loadReservationsAsync() {
        if (loadingIndicator != null) loadingIndicator.setVisible(true);

        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                Thread.sleep(400);
                List<String> items = new ArrayList<>();
                for (Reservation r : HotelDatabase.reservations) {
                    if (r.getGuest().getUsername().equals(guest.getUsername())) {
                        boolean isPaid = false;
                        for (Invoice inv : HotelDatabase.invoices) {
                            if (inv.isPaid() &&
                                    inv.getReservation().getGuest().getUsername().equals(r.getGuest().getUsername()) &&
                                    inv.getReservation().getRoom().getRoomId() == r.getRoom().getRoomId()) {
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
                if (items.isEmpty()) items.add("No reservations yet.");
                return items;
            }
        };

        task.setOnSucceeded(event -> {
            reservationsList.setItems(FXCollections.observableArrayList(task.getValue()));
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            updateUI();
        });

        task.setOnFailed(event -> {
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            messageLabel.setText("Error loading reservations.");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown())
            scheduler.shutdown();
    }

    @FXML
    private void handleBrowseRooms() {
        stopScheduler();
        SceneManager.switchTo("RoomBrowsing.fxml");
    }

    @FXML
    private void handleCheckout() {
        Reservation activeRes = null;
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuest().getUsername().equals(guest.getUsername()) && r.isActive()) {
                boolean isPaid = false;
                for (Invoice inv : HotelDatabase.invoices) {
                    if (inv.isPaid() &&
                            inv.getReservation().getGuest().getUsername().equals(r.getGuest().getUsername()) &&
                            inv.getReservation().getRoom().getRoomId() == r.getRoom().getRoomId()) {
                        isPaid = true;
                        break;
                    }
                }
                if (!isPaid) {
                    activeRes = r;
                    break;
                }
            }
        }

        if (activeRes == null) {
            messageLabel.setText("No active unpaid reservation found. You are all set!");
            return;
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                activeRes.getCheckInDate(), activeRes.getCheckOutDate());
        if (nights == 0) nights = 1;
        double total = nights * activeRes.getRoom().getPricePerNight();

        SceneManager.setPendingReservation(activeRes);
        SceneManager.setPendingAmount(total);
        stopScheduler();
        SceneManager.switchTo("Checkout.fxml");
    }

    @FXML
    private void handleCancel() {
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
                + toCancel.getRoom().getRoomNumber() + "?\n"
                + "Any paid amount will be refunded to your balance.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Invoice paidInvoice = null;
            for (Invoice inv : HotelDatabase.invoices)
                if (inv.isPaid() &&
                        inv.getReservation().getGuest().getUsername().equals(toCancel.getGuest().getUsername()) &&
                        inv.getReservation().getRoom().getRoomId() == toCancel.getRoom().getRoomId())
                    paidInvoice = inv;

            if (paidInvoice != null) {
                double refundAmount = paidInvoice.getAmount();
                guest.setBalance(guest.getBalance() + refundAmount);
                HotelDatabase.updateGuestBalance(guest);
                messageLabel.setText("Reservation cancelled. $" + refundAmount + " refunded.");
            } else {
                messageLabel.setText("Reservation cancelled successfully.");
            }

            toCancel.cancel();
            toCancel.getRoom().setAvailable(true);
            HotelDatabase.updateReservationStatus(toCancel);
            HotelDatabase.updateRoomAvailability(toCancel.getRoom());
            updateUI();
            loadReservationsAsync();
        }
    }

    @FXML
    private void handleLogout() {
        stopScheduler();
        SceneManager.setCurrentGuest(null);
        SceneManager.setCurrentStaff(null);
        SceneManager.switchTo("login.fxml");
    }

    @FXML
    private void handleOpenChat() {
        SceneManager.openChat();
    }
}