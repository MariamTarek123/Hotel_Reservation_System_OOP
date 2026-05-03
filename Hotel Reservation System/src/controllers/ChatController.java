package controllers;

import chat.ChatClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.SceneManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML private Text chatTitle;
    @FXML private Label statusLabel;
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;

    private ChatClient client;
    private String username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // figure out who is logged in
        if (SceneManager.getCurrentGuest() != null)
            username = SceneManager.getCurrentGuest().getUsername();
        else if (SceneManager.getCurrentStaff() != null)
            username = SceneManager.getCurrentStaff().getUsername();
        else
            username = "Unknown";

        chatTitle.setText("Live Chat — " + username);

        client = new ChatClient();
        client.connect(username, message -> {
            chatArea.appendText(message + "\n");
        });

        statusLabel.setText("Connected as: " + username);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void handleSend() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;
        client.sendMessage("[" + username + "]: " + text);
        messageField.clear();
    }

    @FXML
    private void handleClose() {
        client.disconnect();
        Stage stage = (Stage) chatArea.getScene().getWindow();
        stage.close();
    }
}