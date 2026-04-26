package main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Guest;
import models.Staff;

public class SceneManager {
    private static Stage stage;
    private static Guest currentGuest;
    private static Staff currentStaff;
    private static models.Room selectedRoom;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void switchTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getClassLoader().getResource(fxmlFile)

            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    SceneManager.class.getResource("/styles/style.css").toExternalForm()
            );
            stage.setScene(scene);
        } catch (Exception e) {
            System.out.println("Error switching to " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Guest
    public static Guest getCurrentGuest() { return currentGuest; }
    public static void setCurrentGuest(Guest guest) { currentGuest = guest; }

    // Staff
    public static Staff getCurrentStaff() { return currentStaff; }
    public static void setCurrentStaff(Staff staff) { currentStaff = staff; }

    // Selected room (passed from room browsing to reservation screen)
    public static models.Room getSelectedRoom() { return selectedRoom; }
    public static void setSelectedRoom(models.Room room) { selectedRoom = room; }
}