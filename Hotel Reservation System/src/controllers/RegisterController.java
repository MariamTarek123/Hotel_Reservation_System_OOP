package controllers;

import database.HotelDatabase;
import enums.genders;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import main.SceneManager;
import models.Guest;
import models.RoomType;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField addressField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> preferenceCombo;
    @FXML private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        genderCombo.setItems(FXCollections.observableArrayList("MALE", "FEMALE"));

        // populate room preferences from database
        for (RoomType rt : HotelDatabase.roomTypes)
            preferenceCombo.getItems().add(rt.getTypeName());
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String address  = addressField.getText().trim();
        LocalDate dob   = dobPicker.getValue();
        String genderStr = genderCombo.getValue();
        String pref     = preferenceCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || address.isEmpty()
                || dob == null || genderStr == null || pref == null) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        // check username not already taken
        if (HotelDatabase.findGuestByUsername(username) != null) {
            errorLabel.setText("Username already taken.");
            return;
        }

        try {
            genders gender = genders.valueOf(genderStr);
            Guest newGuest = new Guest(username, password, dob, address, gender, 2000.0, pref);
            HotelDatabase.guests.add(newGuest);
            HotelDatabase.saveGuest(newGuest);
            SceneManager.setCurrentGuest(newGuest);
            SceneManager.switchTo("dashboard.fxml");
        } catch (IllegalArgumentException e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchTo("login.fxml");
    }
}
