package controllers;

import database.HotelDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import main.SceneManager;
import models.Room;

public class RoomBrowsingController {

    @FXML
    private ListView<Room> roomsListView;
    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        ObservableList<Room> availableRooms = FXCollections.observableArrayList();
        for (Room room : HotelDatabase.rooms) {
            if (room.isAvailable()) {
                availableRooms.add(room);
            }
        }
        
        roomsListView.setItems(availableRooms);
        
        roomsListView.setCellFactory(param -> new ListCell<Room>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) {
                    setText(null);
                } else {
                    setText("Room " + room.getRoomNumber() + " - " + 
                            room.getRoomType().getTypeName() + 
                            " ($" + room.getPricePerNight() + "/night) Floor: " + room.getFloor());
                }
            }
        });
    }

    @FXML
    public void handleBookRoom() {
        Room selectedRoom = roomsListView.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            messageLabel.setText("Please select a room to book.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        SceneManager.setSelectedRoom(selectedRoom);
        SceneManager.switchTo("reservation.fxml");
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("Dashboard.fxml");
    }
}
