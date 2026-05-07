package controllers;

import database.HotelDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import main.SceneManager;
import models.Amenity;
import models.Room;

import java.util.List;
import java.util.stream.Collectors;

public class RoomBrowsingController {

    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private ComboBox<String> priceRangeCombo;
    @FXML private ComboBox<String> floorCombo;
    @FXML private ComboBox<String> sortByCombo;
    @FXML private FlowPane roomsFlowPane;
    @FXML private Label availableCountLabel;
    @FXML private Label bottomStatusLabel;
    @FXML private Label messageLabel;

    private Room selectedRoom = null;
    private VBox selectedRoomCard = null;
    private List<Room> allAvailableRooms;

    @FXML
    public void initialize() {
        // Populate ComboBoxes
        roomTypeCombo.setItems(FXCollections.observableArrayList("All Types", "Single", "Double", "Suite"));
        priceRangeCombo.setItems(FXCollections.observableArrayList("All Prices", "< $100", "$100 - $150", "> $150"));
        floorCombo.setItems(FXCollections.observableArrayList("All Floors", "Floor 1", "Floor 2", "Floor 3"));
        sortByCombo.setItems(FXCollections.observableArrayList("Price: Low to High", "Price: High to Low"));

        // Add Listeners
        roomTypeCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        priceRangeCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        floorCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        sortByCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilters());

        // Collect available rooms
        allAvailableRooms = HotelDatabase.rooms.stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());

        applyFilters();
    }

    private void applyFilters() {
        List<Room> filtered = allAvailableRooms.stream().filter(room -> {
            // Room Type
            String rt = roomTypeCombo.getValue();
            if (rt != null && !rt.equals("All Types") && !room.getRoomType().getTypeName().equals(rt)) {
                return false;
            }
            // Price Range
            String pr = priceRangeCombo.getValue();
            if (pr != null && !pr.equals("All Prices")) {
                if (pr.equals("< $100") && room.getPricePerNight() >= 100) return false;
                if (pr.equals("$100 - $150") && (room.getPricePerNight() < 100 || room.getPricePerNight() > 150)) return false;
                if (pr.equals("> $150") && room.getPricePerNight() <= 150) return false;
            }
            // Floor
            String fl = floorCombo.getValue();
            if (fl != null && !fl.equals("All Floors")) {
                if (fl.equals("Floor 1") && room.getFloor() != 1) return false;
                if (fl.equals("Floor 2") && room.getFloor() != 2) return false;
                if (fl.equals("Floor 3") && room.getFloor() != 3) return false;
            }
            return true;
        }).collect(Collectors.toList());

        // Sorting
        String sort = sortByCombo.getValue();
        if (sort != null && sort.equals("Price: High to Low")) {
            filtered.sort((r1, r2) -> Double.compare(r2.getPricePerNight(), r1.getPricePerNight()));
        } else {
            // Default or "Low to High"
            filtered.sort((r1, r2) -> Double.compare(r1.getPricePerNight(), r2.getPricePerNight()));
        }

        availableCountLabel.setText(filtered.size() + " rooms available");
        selectedRoom = null;
        selectedRoomCard = null;
        updateBottomStatus();
        populateRooms(filtered);
    }

    private void populateRooms(List<Room> rooms) {
        roomsFlowPane.getChildren().clear();
        for (Room room : rooms) {
            VBox card = createRoomCard(room);
            roomsFlowPane.getChildren().add(card);
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox();
        card.setPrefWidth(350);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Top Image Placeholder
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.setPrefHeight(150);
        imagePlaceholder.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 8 8 0 0;");

        ImageView roomImageView = new ImageView(loadRoomImage(room));
        roomImageView.setFitWidth(350);
        roomImageView.setFitHeight(150);
        roomImageView.setPreserveRatio(false);
        roomImageView.setSmooth(true);

        Label roomNumberLabel = new Label("Room " + room.getRoomNumber());
        roomNumberLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Merriweather';");
        StackPane.setAlignment(roomNumberLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(roomNumberLabel, new Insets(10));

        Label ratingLabel = new Label("★ 4.5");
        ratingLabel.setStyle("-fx-background-color: white; -fx-text-fill: #333333; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");
        StackPane.setAlignment(ratingLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(ratingLabel, new Insets(10));

        imagePlaceholder.getChildren().addAll(roomImageView, roomNumberLabel, ratingLabel);

        // Middle Info
        HBox infoBox = new HBox();
        infoBox.setPadding(new Insets(15));
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label(room.getRoomType().getTypeName());
        typeLabel.setStyle("-fx-background-color: #ffe6e6; -fx-text-fill: #cc0000; -fx-padding: 3 8; -fx-font-size: 12px; -fx-background-radius: 4;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox priceBox = new VBox();
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        Label priceLabel = new Label(String.format("$%.0f", room.getPricePerNight()));
        priceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Merriweather'; -fx-text-fill: #1a1a1a;");
        Label perNightLabel = new Label("per night");
        perNightLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        priceBox.getChildren().addAll(priceLabel, perNightLabel);

        infoBox.getChildren().addAll(typeLabel, spacer, priceBox);

        // Stats Row (Floor, Guests, Size)
        HBox statsBox = new HBox(10);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(0, 15, 15, 15));

        statsBox.getChildren().addAll(
                createStatItem("Floor " + room.getFloor()),
                createStatItem("2 guests"), // Hardcoded for layout match
                createStatItem("32 m²")    // Hardcoded for layout match
        );

        // Amenities
        VBox amenitiesBox = new VBox(5);
        amenitiesBox.setPadding(new Insets(0, 15, 15, 15));
        Label amLabel = new Label("Amenities");
        amLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");

        FlowPane chipsPane = new FlowPane();
        chipsPane.setHgap(5);
        chipsPane.setVgap(5);

        for (Amenity amenity : room.getAmenities()) {
            Label chip = new Label(amenity.getName());
            chip.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #555555; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px;");
            chipsPane.getChildren().add(chip);
        }

        amenitiesBox.getChildren().addAll(amLabel, chipsPane);

        card.getChildren().addAll(imagePlaceholder, infoBox, statsBox, amenitiesBox);

        // Selection Logic
        card.setOnMouseClicked(e -> {
            if (selectedRoomCard != null) {
                selectedRoomCard.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            }
            selectedRoomCard = card;
            selectedRoom = room;
            card.setStyle("-fx-background-color: white; -fx-border-color: #cc0000; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(204,0,0,0.3), 10, 0, 0, 5);");

            messageLabel.setText("");
            updateBottomStatus();
        });

        return card;
    }

    private VBox createStatItem(String text) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #fafafa; -fx-padding: 10; -fx-background-radius: 4;");
        box.setPrefWidth(95);

        Label icon = new Label("•"); // Simple text icon placeholder
        icon.setStyle("-fx-text-fill: #cc0000;");
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

        box.getChildren().addAll(icon, label);
        return box;
    }

    private Image loadRoomImage(Room room) {
        try {
            String typeName = "room";
            if (room != null && room.getRoomType() != null && room.getRoomType().getTypeName() != null) {
                // E.g. "Double", "Single", "Suite"
                typeName = room.getRoomType().getTypeName().replaceAll("\\s+", "").toLowerCase();
            }

            // Load from classpath — images/ folder must be inside src/
            java.io.InputStream stream = getClass().getResourceAsStream("/images/" + typeName + ".jpg");
            if (stream != null) {
                return new Image(stream);
            }

            // Fallback to back1.jpg
            java.io.InputStream fallback = getClass().getResourceAsStream("/images/back1.jpg");
            if (fallback != null) {
                return new Image(fallback);
            }
        } catch (Exception ignored) {
        }

        // Final fallback
        return new Image("https://via.placeholder.com/350x150.png?text=Room+Photo", true);
    }

    private void updateBottomStatus() {
        String selection = selectedRoom != null ? "Selected Room " + selectedRoom.getRoomNumber() : "No selection";
        bottomStatusLabel.setText(allAvailableRooms.size() + " rooms available | " + selection);
    }

    @FXML
    public void handleBookRoom() {
        if (selectedRoom == null) {
            messageLabel.setText("Please select a room to book.");
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