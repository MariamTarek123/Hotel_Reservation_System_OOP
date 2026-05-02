package main;

import database.HotelDatabase;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //HotelDatabase.populate();
        SceneManager.setStage(stage);
        SceneManager.switchTo("login.fxml");
        stage.setTitle("Hotel Reservation System");
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}