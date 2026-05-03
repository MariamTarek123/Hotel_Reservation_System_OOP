package chat;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5001;
    private static List<PrintWriter> clients = new ArrayList<>();
    private static boolean running = false;

    public static void start() {
        if (running) return;
        running = true;

        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Chat server started on port " + PORT);
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    synchronized (clients) { clients.add(out); }
                    System.out.println("New client connected.");

                    Thread clientThread = new Thread(() -> {
                        try (BufferedReader in = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()))) {
                            String message;
                            while ((message = in.readLine()) != null) {
                                broadcast(message, out);
                            }
                        } catch (IOException e) {
                            System.out.println("Client disconnected.");
                        } finally {
                            synchronized (clients) { clients.remove(out); }
                        }
                    });
                    clientThread.setDaemon(true);
                    clientThread.start();
                }
            } catch (IOException e) {
                System.out.println("Server error: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private static void broadcast(String message, PrintWriter sender) {
        synchronized (clients) {
            for (PrintWriter client : clients)
                client.println(message);
        }
    }

    public static void stop() { running = false; }
}