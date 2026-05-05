package chat;

import javafx.application.Platform;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5001;

    private Socket socket;
    private PrintWriter out;
    private MessageListener listener;

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public void connect(String username, MessageListener listener) {
        this.listener = listener;
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);

            Thread receiveThread = new Thread(() -> {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))) {
                    String message;
                    while ((message = in.readLine()) != null) {
                        final String msg = message;
                        Platform.runLater(() -> listener.onMessageReceived(msg));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> listener.onMessageReceived("-- Disconnected --"));
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            System.out.println(username + " connected to chat server.");
        } catch (IOException e) {
            System.out.println("Could not connect to chat server: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error disconnecting: " + e.getMessage());
        }
    }
}
