package controllers;

import exceptions.RoomNotAvailableException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import main.SceneManager;
import models.Amenity;
import models.Guest;
import models.Reservation;
import models.Room;
import database.HotelDatabase;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    @FXML private Label roomNumberLabel;
    @FXML private Label roomTypeLabel;
    @FXML private Label roomPriceLabel;
    @FXML private Label roomFloorLabel;
    @FXML private Label roomAmenitiesLabel;
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private Label summaryLabel;
    @FXML private Label errorLabel;

    private Room room;
    private Guest guest;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        room  = SceneManager.getSelectedRoom();
        guest = SceneManager.getCurrentGuest();

        if (room != null) {
            roomNumberLabel.setText("Room: " + room.getRoomNumber());
            roomTypeLabel.setText("Type: " + room.getRoomType().getTypeName());
            roomPriceLabel.setText("Price: $" + room.getPricePerNight() + " / night");
            roomFloorLabel.setText("Floor: " + room.getFloor());

            StringBuilder amenities = new StringBuilder("Amenities: ");
            for (int i = 0; i < room.getAmenities().size(); i++) {
                amenities.append(room.getAmenities().get(i).getName());
                if (i < room.getAmenities().size() - 1) amenities.append(", ");
            }
            roomAmenitiesLabel.setText(amenities.toString());
        }
    }

    @FXML
    private void handleCalculate() {
        errorLabel.setText("");
        LocalDate checkIn  = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        if (checkIn == null || checkOut == null) {
            errorLabel.setText("Please select both dates.");
            return;
        }
        if (!checkOut.isAfter(checkIn)) {
            errorLabel.setText("Check-out must be after check-in.");
            return;
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double total = nights * room.getPricePerNight();
        summaryLabel.setText("Nights: " + nights + "  |  Total: $" + total);
    }

    @FXML
    private void handleConfirm() {
        errorLabel.setText("");
        LocalDate checkIn  = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        if (checkIn == null || checkOut == null) {
            errorLabel.setText("Please select both dates.");
            return;
        }
        if (!checkOut.isAfter(checkIn)) {
            errorLabel.setText("Check-out must be after check-in.");
            return;
        }

        try {
            Reservation newRes = guest.makeReservation(room, checkIn, checkOut);


            database.HotelDatabase.saveReservation(newRes);
            database.HotelDatabase.updateRoomAvailability(room);

            // Calculate total price
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            double total = nights * room.getPricePerNight();

            SceneManager.setPendingReservation(newRes);
            SceneManager.setPendingAmount(total);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Dates confirmed! Please proceed to payment.");
            SceneManager.switchTo("Checkout.fxml");
        } catch (RoomNotAvailableException e) {
            errorLabel.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Unexpected error: " + e.getMessage());
        }
    }
    @FXML
    private void handleBack() {
        SceneManager.switchTo("roomBrowsing.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
