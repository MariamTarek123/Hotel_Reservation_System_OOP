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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

public class StaffDashboardController implements Initializable {

    @FXML private Text welcomeText;
    @FXML private Label roleLabel;
    @FXML private Label messageLabel;

    @FXML private ListView<Guest> guestsListView;
    @FXML private ListView<Room> roomsListView;
    @FXML private ListView<Reservation> reservationsListView;

    @FXML private Label guestCountLabel;
    @FXML private Label roomCountLabel;
    @FXML private Label reservationCountLabel;
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

    @FXML
    private void handleCreateReceptionist() {
        if (!(currentStaff instanceof Admin)) return;

        Dialog<Receptionist> dialog = new Dialog<>();
        dialog.setTitle("Create New Receptionist");
        dialog.setHeaderText("Enter receptionist details:");

        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField usernameField  = new TextField();
        PasswordField passField  = new PasswordField();
        TextField addressField   = new TextField();
        TextField dobField       = new TextField();
        dobField.setPromptText("YYYY-MM-DD");
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("MALE", "FEMALE");
        TextField hoursField     = new TextField();

        grid.add(new Label("Username:"),     0, 0); grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"),     0, 1); grid.add(passField,     1, 1);
        grid.add(new Label("Address:"),      0, 2); grid.add(addressField,  1, 2);
        grid.add(new Label("Date of Birth:"),0, 3); grid.add(dobField,      1, 3);
        grid.add(new Label("Gender:"),       0, 4); grid.add(genderBox,     1, 4);
        grid.add(new Label("Working Hours:"),0, 5); grid.add(hoursField,    1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createButton) {
                try {
                    Receptionist rec = new Receptionist(
                            usernameField.getText().trim(),
                            passField.getText(),
                            java.time.LocalDate.parse(dobField.getText().trim()),
                            addressField.getText().trim(),
                            enums.genders.valueOf(genderBox.getValue()),
                            Integer.parseInt(hoursField.getText().trim())
                    );
                    return rec;
                } catch (Exception e) {
                    messageLabel.setText("Error: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(rec -> {
            HotelDatabase.staff.add(rec);
            HotelDatabase.saveStaff(rec);
            messageLabel.setText("Receptionist " + rec.getUsername() + " created successfully.");
        });
    }

    @FXML
    private void handleOpenChat() {
        SceneManager.openChat();
    }

    private void refreshLists() {
        if (guestCountLabel != null) guestCountLabel.setText(String.valueOf(database.HotelDatabase.guests.size()));
        if (roomCountLabel != null) roomCountLabel.setText(String.valueOf(database.HotelDatabase.rooms.size()));
        long activeRes = database.HotelDatabase.reservations.stream()
            .filter(r -> r.getStatus() == enums.reservationstatus.CONFIRMED || r.getStatus() == enums.reservationstatus.PENDING)
            .count();
        if (reservationCountLabel != null) reservationCountLabel.setText(String.valueOf(activeRes));
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
