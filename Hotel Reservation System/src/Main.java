import database.HotelDatabase;
import enums.genders;
import enums.paymentmethod;
import exceptions.InvalidCredentialsException;
import exceptions.InvalidDateRangeException;
import exceptions.InvalidPaymentException;
import exceptions.RoomNotAvailableException;
import models.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {
       // HotelDatabase.populate();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   Welcome to Hotel Reservation System ║");
        System.out.println("╚══════════════════════════════════════╝");

        while (true) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Login as Guest");
            System.out.println("2. Login as Staff");
            System.out.println("3. Register as New Guest");
            System.out.println("4. Exit");
            System.out.print("Your choice: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1 -> guestLogin();
                case 2 -> staffLogin();
                case 3 -> registerGuest();
                case 4 -> {
                    System.out.println("Thank you for using our system. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter 1-4.");
            }
        }
    }

    // ===== GUEST LOGIN =====
    static void guestLogin() {
        System.out.println("\n--- Guest Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Guest guest = HotelDatabase.findGuestByUsername(username);
        if (guest == null) {
            System.out.println("No guest found with that username.");
            return;
        }
        try {
            guest.login(username, password);
            System.out.println("\nWelcome back, " + guest.getUsername() + "!");
            guestMenu(guest);
        } catch (InvalidCredentialsException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    // ===== GUEST MENU =====
    static void guestMenu(Guest guest) {
        while (true) {
            System.out.println("\n===== GUEST MENU =====");
            System.out.println("1. View available rooms");
            System.out.println("2. Make a reservation");
            System.out.println("3. View my reservations");
            System.out.println("4. Cancel a reservation");
            System.out.println("5. Pay invoice");
            System.out.println("6. View my balance");
            System.out.println("7. Logout");
            System.out.print("Your choice: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1 -> guest.viewAvailableRooms();
                case 2 -> makeReservation(guest);
                case 3 -> guest.viewReservations();
                case 4 -> cancelReservation(guest);
                case 5 -> payInvoice(guest);
                case 6 -> System.out.println("Your current balance: $" + guest.getBalance());
                case 7 -> {
                    System.out.println("Logged out successfully.");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter 1-7.");
            }
        }
    }

    // ===== MAKE RESERVATION =====
    static void makeReservation(Guest guest) {
        System.out.println("\n--- Available Rooms ---");
        boolean anyAvailable = false;
        for (Room r : HotelDatabase.rooms) {
            if (r.isAvailable()) {
                anyAvailable = true;
                System.out.println("Room " + r.getRoomNumber()
                        + " | Type: " + r.getRoomType().getTypeName()
                        + " | Floor: " + r.getFloor()
                        + " | Price: $" + r.getPricePerNight() + "/night");
                System.out.print("  Amenities: ");
                for (int i = 0; i < r.getAmenities().size(); i++) {
                    System.out.print(r.getAmenities().get(i).getName());
                    if (i < r.getAmenities().size() - 1) System.out.print(", ");
                }
                System.out.println();
            }
        }
        if (!anyAvailable) {
            System.out.println("No rooms available at the moment.");
            return;
        }

        System.out.print("\nEnter room number to book: ");
        String roomNumber = scanner.nextLine();

        Room selectedRoom = null;
        for (Room r : HotelDatabase.rooms)
            if (r.getRoomNumber().equals(roomNumber))
                selectedRoom = r;

        if (selectedRoom == null) {
            System.out.println("Room number not found.");
            return;
        }
        if (!selectedRoom.isAvailable()) {
            System.out.println("Sorry, room " + roomNumber + " is not available.");
            return;
        }

        LocalDate checkIn = null;
        LocalDate checkOut = null;
        while (checkIn == null) {
            System.out.print("Check-in date  (DD-MM-YYYY): ");
            try {
                checkIn = LocalDate.parse(scanner.nextLine(), dateFormatter);
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Invalid date. Please check the year, month, and day and use DD-MM-YYYY format.");
            }
        }

        while (checkOut == null) {
            System.out.print("Check-out date (DD-MM-YYYY): ");
            try {
                checkOut = LocalDate.parse(scanner.nextLine(), dateFormatter);
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Invalid date. Please check the year, month, and day and use DD-MM-YYYY format.");
            }
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double total = nights * selectedRoom.getPricePerNight();
        System.out.println("\n--- Reservation Summary ---");
        System.out.println("Room    : " + selectedRoom.getRoomNumber() + " (" + selectedRoom.getRoomType().getTypeName() + ")");
        System.out.println("Nights  : " + nights);
        System.out.println("Total   : $" + total);
        System.out.print("Confirm reservation? (yes/no): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            try {
                guest.makeReservation(selectedRoom, checkIn, checkOut);
            } catch (RoomNotAvailableException | InvalidDateRangeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Reservation cancelled.");
        }
    }

    // ===== CANCEL RESERVATION =====
    static void cancelReservation(Guest guest) {
        System.out.println("\n--- Your Active Reservations ---");
        boolean found = false;
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuest().getUsername().equals(guest.getUsername()) && r.isActive()) {
                found = true;
                System.out.println("Room: " + r.getRoom().getRoomNumber()
                        + " | Check-in: " + r.getCheckInDate()
                        + " | Check-out: " + r.getCheckOutDate()
                        + " | Status: " + r.getStatus());
            }
        }
        if (!found) {
            System.out.println("You have no active reservations.");
            return;
        }

        System.out.print("Enter room number to cancel: ");
        String roomNumber = scanner.nextLine();
        boolean cancelled = false;
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuest().getUsername().equals(guest.getUsername())
                    && r.getRoom().getRoomNumber().equals(roomNumber)
                    && r.isActive()) {
                r.cancel();
                r.getRoom().setAvailable(true);
                System.out.println("Reservation for room " + roomNumber + " has been cancelled.");
                cancelled = true;
                break;
            }
        }
        if (!cancelled)
            System.out.println("No active reservation found for that room number.");
    }

    // ===== PAY INVOICE =====
    static void payInvoice(Guest guest) {
        System.out.println("\n--- Pay Invoice ---");
        System.out.println("Your current balance: $" + guest.getBalance());
        System.out.print("Enter amount to pay: $");
        double amount = 0;
        try {
            amount = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered. Cannot process payment.");
            return;
        }

        System.out.println("\nPayment Methods:");
        System.out.println("1. Cash");
        System.out.println("2. Credit Card");
        System.out.println("3. Online");
        System.out.print("Choose payment method (1-3): ");
        int methodChoice = -1;
        try {
            methodChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Defaulting to Cash.");
        }

        paymentmethod method = switch (methodChoice) {
            case 1 -> paymentmethod.CASH;
            case 2 -> paymentmethod.CREDIT_CARD;
            case 3 -> paymentmethod.ONLINE;
            default -> {
                System.out.println("Invalid choice, defaulting to Cash.");
                yield paymentmethod.CASH;
            }
        };

        try {
            guest.pay(amount, method);
            System.out.println("Payment successful! Remaining balance: $" + guest.getBalance());
        } catch (InvalidPaymentException e) {
            System.out.println("Payment failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ===== STAFF LOGIN =====
    static void staffLogin() {
        System.out.println("\n--- Staff Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        for (Staff s : HotelDatabase.staff) {
            try {
                s.login(username, password);
                System.out.println("\nWelcome, " + s.getUsername() + "! Role: " + s.getStaffRole());
                staffMenu(s);
                return;
            } catch (InvalidCredentialsException e) {
                // try next staff member
            }
        }
        System.out.println("Staff member not found or incorrect password.");
    }

    // ===== STAFF MENU =====
    static void staffMenu(Staff staff) {
        while (true) {
            System.out.println("\n===== STAFF MENU =====");
            System.out.println("1. View all guests");
            System.out.println("2. View all rooms");
            System.out.println("3. View all reservations");
            if (staff instanceof Receptionist) {
                System.out.println("4. Check-in guest");
                System.out.println("5. Check-out guest");
            }
            System.out.println("6. Logout");
            System.out.print("Your choice: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.println("\n--- All Guests ---");
                    staff.viewAllGuests();
                }
                case 2 -> {
                    System.out.println("\n--- All Rooms ---");
                    staff.viewAllRooms();
                }
                case 3 -> {
                    System.out.println("\n--- All Reservations ---");
                    staff.viewAllReservations();
                }
                case 4 -> {
                    if (staff instanceof Receptionist rec) {
                        System.out.println("\n--- Pending Reservations ---");
                        for (Reservation r : HotelDatabase.reservations)
                            System.out.println("Guest: " + r.getGuest().getUsername()
                                    + " | Room: " + r.getRoom().getRoomNumber()
                                    + " | Status: " + r.getStatus());
                        System.out.print("Enter guest username to check-in: ");
                        String username = scanner.nextLine();
                        boolean found = false;
                        for (Reservation r : HotelDatabase.reservations)
                            if (r.getGuest().getUsername().equals(username)) {
                                rec.manageCheckIn(r);
                                found = true;
                                break;
                            }
                        if (!found) System.out.println("No reservation found for that guest.");
                    }
                }
                case 5 -> {
                    if (staff instanceof Receptionist rec) {
                        System.out.println("\n--- Confirmed Reservations ---");
                        for (Reservation r : HotelDatabase.reservations)
                            System.out.println("Guest: " + r.getGuest().getUsername()
                                    + " | Room: " + r.getRoom().getRoomNumber()
                                    + " | Status: " + r.getStatus());
                        System.out.print("Enter guest username to check-out: ");
                        String username = scanner.nextLine();
                        boolean found = false;
                        for (Reservation r : HotelDatabase.reservations)
                            if (r.getGuest().getUsername().equals(username)) {
                                rec.manageCheckOut(r);
                                found = true;
                                break;
                            }
                        if (!found) System.out.println("No reservation found for that guest.");
                    }
                }
                case 6 -> {
                    System.out.println("Logged out successfully.");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    // ===== REGISTER GUEST =====
    static void registerGuest() {
        System.out.println("\n--- New Guest Registration ---");
        System.out.print("Username (min 3 characters): ");
        String username = scanner.nextLine();
        System.out.print("Password (min 7 chars, must include letters and digits): ");
        String password = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        
        LocalDate dob = null;
        while (dob == null) {
            System.out.print("Date of birth (DD-MM-YYYY): ");
            try {
                dob = LocalDate.parse(scanner.nextLine(), dateFormatter);
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Invalid date. Please check the year, month, and day and use DD-MM-YYYY format.");
            }
        }

        System.out.println("Gender options:");
        System.out.println("1. Male");
        System.out.println("2. Female");
        System.out.print("Choose (1-2): ");
        int genderChoice = 1;
        try {
            genderChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Defaulting to Male.");
        }
        genders gender = genderChoice == 2 ? genders.FEMALE : genders.MALE;

        System.out.println("\nRoom Type Preferences:");
        for (RoomType rt : HotelDatabase.roomTypes)
            System.out.println("- " + rt.getTypeName() + " ($" + rt.getBasePrice() + "/night)");
        System.out.print("Enter preferred room type: ");
        String prefs = scanner.nextLine();

        try {
            Guest newGuest = new Guest(username, password, dob, address, gender, 0.0, prefs);
            HotelDatabase.guests.add(newGuest);
            System.out.println("\nRegistration successful! You can now login with username: " + username);
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }
}

