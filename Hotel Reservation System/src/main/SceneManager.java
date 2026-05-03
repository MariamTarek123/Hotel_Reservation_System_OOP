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
    private static models.Reservation pendingReservation;
    private static double pendingAmount;

    // ── Theme state ────────────────────────────────────────────
    /** true = dark mode (default);  false = light mode */
    private static boolean darkMode = true;

    /**
     * Toggle between dark and light mode.
     * Call this from any controller, e.g. on a button click:
     *   SceneManager.toggleTheme();
     */
    public static void toggleTheme() {
        darkMode = !darkMode;
        applyTheme(stage.getScene());
    }

    /** Returns true if the app is currently in dark mode. */
    public static boolean isDarkMode() { return darkMode; }

    /** Applies the current theme to the given scene. */
    private static void applyTheme(Scene scene) {
        if (scene == null) return;
        Parent root = scene.getRoot();
        if (darkMode) {
            root.getStyleClass().remove("light-mode");
        } else {
            if (!root.getStyleClass().contains("light-mode")) {
                root.getStyleClass().add("light-mode");
            }
        }
    }
    // ──────────────────────────────────────────────────────────

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
            // Try both common locations so the CSS loads regardless of project layout
            java.net.URL cssUrl = SceneManager.class.getResource("/style.css");
            if (cssUrl == null) cssUrl = SceneManager.class.getResource("/styles/style.css");
            if (cssUrl == null) cssUrl = SceneManager.class.getClassLoader().getResource("style.css");
            if (cssUrl == null) cssUrl = SceneManager.class.getClassLoader().getResource("styles/style.css");
            if (cssUrl == null) {
                java.io.File file = new java.io.File("src/style.css");
                if (file.exists()) {
                    cssUrl = file.toURI().toURL();
                }
            }
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Warning: style.css not found. Tried /style.css and /styles/style.css");
            }
            // Apply persisted theme preference
            applyTheme(scene);
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

    // Pending reservation for Checkout
    public static models.Reservation getPendingReservation() { return pendingReservation; }
    public static void setPendingReservation(models.Reservation res) { pendingReservation = res; }

    public static double getPendingAmount() { return pendingAmount; }
    public static void setPendingAmount(double amount) { pendingAmount = amount; }


    public static void openChat() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getClassLoader().getResource("chat.fxml")
            );
            Parent root = loader.load();
            Stage chatStage = new Stage();
            chatStage.setTitle("Live Chat");
            Scene scene = new Scene(root);
            chatStage.setScene(scene);
            chatStage.setResizable(false);
            chatStage.show();
        } catch (Exception e) {
            System.out.println("Error opening chat: " + e.getMessage());
            e.printStackTrace();
        }
    }
}