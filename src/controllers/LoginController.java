package controllers;

import database.HotelDatabase;
import exceptions.InvalidCredentialsException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.SceneManager;
import models.Guest;
import models.Staff;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        Guest guest = HotelDatabase.findGuestByUsername(username);
        if (guest == null) {
            errorLabel.setText("No guest found with that username.");
            return;
        }

        try {
            guest.login(username, password);
            SceneManager.setCurrentGuest(guest);
            SceneManager.switchTo("dashboard.fxml");
        } catch (InvalidCredentialsException e) {
            errorLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void handleStaffLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        for (Staff s : HotelDatabase.staff) {
            try {
                s.login(username, password);
                SceneManager.setCurrentStaff(s);
                SceneManager.switchTo("staff_dashboard.fxml");
                return;
            } catch (InvalidCredentialsException e) {
                // try next
            }
        }
        errorLabel.setText("Staff member not found or wrong password.");
    }

    @FXML
    private void goToRegister() {
        SceneManager.switchTo("register.fxml");
    }
}