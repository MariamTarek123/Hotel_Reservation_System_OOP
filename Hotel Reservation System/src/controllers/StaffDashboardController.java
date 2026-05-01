package controllers;

import database.HotelDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import main.SceneManager;
import models.*;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StaffDashboardController implements Initializable {

    @FXML private Text welcomeText;
    @FXML private Label roleLabel;
    @FXML private Label messageLabel;

    @FXML private ListView<Guest> guestsListView;
    @FXML private ListView<Room> roomsListView;
    @FXML private ListView<Reservation> reservationsListView;

    @FXML private HBox adminRoomsBox;
    @FXML private HBox receptionistBox;

    private Staff currentStaff;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentStaff = SceneManager.getCurrentStaff();
        if (currentStaff == null) return;

        welcomeText.setText("Dashboard - " + currentStaff.getUsername());

        // Use instanceof to check role based on OOP constraints
        if (currentStaff instanceof Admin) {
            roleLabel.setText("Role: ADMIN");
            adminRoomsBox.setVisible(true);
            adminRoomsBox.setManaged(true);
        } else if (currentStaff instanceof Receptionist) {
            roleLabel.setText("Role: RECEPTIONIST");
            receptionistBox.setVisible(true);
            receptionistBox.setManaged(true);
        }

        refreshLists();
    }

    private void refreshLists() {
        guestsListView.setItems(FXCollections.observableArrayList(HotelDatabase.guests));
        roomsListView.setItems(FXCollections.observableArrayList(HotelDatabase.rooms));
        reservationsListView.setItems(FXCollections.observableArrayList(HotelDatabase.reservations));
    }

    @FXML
    private void handleLogout() {
        SceneManager.setCurrentStaff(null);
        SceneManager.switchTo("login.fxml");
    }

    // --- ADMIN ACTIONS (simplified dummy operations to satisfy basic objective) ---

    @FXML
    private void handleCreateRoom() {
        if (!(currentStaff instanceof Admin admin)) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Room");
        dialog.setHeaderText("Create a new Single Room for testing.");
        dialog.setContentText("Enter Room Number:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(roomNum -> {
            RoomType rt = HotelDatabase.roomTypes.get(0); // single
            Room newRoom = new Room(HotelDatabase.rooms.size() + 1, roomNum, 1, rt.getBasePrice(), rt);
            admin.create(newRoom);
            refreshLists();
            messageLabel.setText("Room " + roomNum + " created.");
        });
    }

    @FXML
    private void handleUpdateRoom() {
        if (!(currentStaff instanceof Admin admin)) return;

        Room selected = roomsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a room to update first.");
            return;
        }

        List<String> choices = Arrays.asList("Price", "Room Number", "Floor", "Type");
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Price", choices);
        choiceDialog.setTitle("Update Room");
        choiceDialog.setHeaderText("Update Room: " + selected.getRoomNumber());
        choiceDialog.setContentText("Choose attribute to modify:");

        Optional<String> result = choiceDialog.showAndWait();
        if (result.isPresent()) {
            String choice = result.get();

            if (choice.equals("Type")) {
                ChoiceDialog<RoomType> typeDialog = new ChoiceDialog<>(selected.getRoomType(), HotelDatabase.roomTypes);
                typeDialog.setTitle("Update Room Type");
                typeDialog.setHeaderText("Current Type: " + selected.getRoomType().getTypeName());
                typeDialog.setContentText("Choose new type:");
                
                Optional<RoomType> newType = typeDialog.showAndWait();
                newType.ifPresent(roomType -> {
                    selected.setRoomType(roomType);
                    admin.update(selected);
                    refreshLists();
                    messageLabel.setText("Room Type updated successfully.");
                });
            } else {
                String defaultValue = "";
                if (choice.equals("Price")) defaultValue = String.valueOf(selected.getPricePerNight());
                else if (choice.equals("Room Number")) defaultValue = selected.getRoomNumber();
                else if (choice.equals("Floor")) defaultValue = String.valueOf(selected.getFloor());

                TextInputDialog inputDialog = new TextInputDialog(defaultValue);
                inputDialog.setTitle("Update " + choice);
                inputDialog.setHeaderText("Updating " + choice + " for Room: " + selected.getRoomNumber());
                inputDialog.setContentText("Enter new " + choice + ":");

                Optional<String> inputResult = inputDialog.showAndWait();
                inputResult.ifPresent(newValue -> {
                    try {
                        switch (choice) {
                            case "Price":
                                selected.setPricePerNight(Double.parseDouble(newValue));
                                break;
                            case "Room Number":
                                selected.setRoomNumber(newValue);
                                break;
                            case "Floor":
                                selected.setFloor(Integer.parseInt(newValue));
                                break;
                        }
                        admin.update(selected);
                        refreshLists();
                        messageLabel.setText("Room " + choice + " updated successfully.");
                    } catch (NumberFormatException e) {
                        messageLabel.setText("Invalid input entered for " + choice + ".");
                    }
                });
            }
        }
    }

    @FXML
    private void handleDeleteRoom() {
        if (!(currentStaff instanceof Admin admin)) return;

        Room selected = roomsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a room to delete first.");
            return;
        }

        admin.delete(selected);
        refreshLists();
        messageLabel.setText("Room deleted.");
    }

    // --- RECEPTIONIST ACTIONS ---

    @FXML
    private void handleCheckIn() {
        if (!(currentStaff instanceof Receptionist rec)) return;

        Reservation selected = reservationsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a reservation to check in.");
            return;
        }

        try {
            rec.manageCheckIn(selected);
            refreshLists();
            messageLabel.setText("Handled Check-in for: " + selected.getGuest().getUsername());
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleCheckOut() {
        if (!(currentStaff instanceof Receptionist rec)) return;

        Reservation selected = reservationsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a reservation to check out.");
            return;
        }

        try {
            rec.manageCheckOut(selected);
            refreshLists();
            messageLabel.setText("Handled Check-out for: " + selected.getGuest().getUsername());
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        }
    }
}
