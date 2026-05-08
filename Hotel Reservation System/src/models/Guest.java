package models;

import database.HotelDatabase;
import enums.genders;
import enums.paymentmethod;
import exceptions.InvalidPaymentException;
import exceptions.RoomNotAvailableException;
import exceptions.InvalidDateRangeException;
import interfaces.Payable;

import java.time.LocalDate;

public class Guest extends Person implements Payable {
    private double balance;
    private String roomPreferences;


    public Guest(String username, String password, LocalDate dateOfBirth, String address, genders gender, double balance, String roomPreferences) {
        super(username, password, dateOfBirth, address, gender);
        if (balance < 0)
            throw new IllegalArgumentException("Balance cannot be negative.");
        this.balance = balance;
        this.roomPreferences = roomPreferences;
    }

    @Override
    public void pay(double amount, paymentmethod method) throws InvalidPaymentException {
        if (amount <= 0)
            throw new InvalidPaymentException("Amount must be positive.");
        if (amount > balance)
            throw new InvalidPaymentException("Insufficient balance.");
        balance -= amount;
        System.out.println("Payment of $" + amount + " made via " + method);
    }

    @Override
    public double getBalance() { return balance; }

    public void viewAvailableRooms() {
        System.out.println("Available Rooms:");
        for (Room r : HotelDatabase.rooms)
            if (r.isAvailable())
                System.out.println("  " + r.getRoomNumber() + " - " + r.getRoomType().getTypeName() + " - $" + r.getPricePerNight() + "/night");
    }

    public Reservation makeReservation(Room room, LocalDate checkIn, LocalDate checkOut) throws RoomNotAvailableException, InvalidDateRangeException {
        if (!room.isAvailable())
            throw new RoomNotAvailableException("Room " + room.getRoomNumber() + " is not available.");
            
        // Check the database to prevent overlapped dates for already booked rooms
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getRoom().getRoomNumber().equals(room.getRoomNumber()) && r.isActive()) {
                if (r.overlaps(checkIn, checkOut)) {
                    throw new RoomNotAvailableException("Room " + room.getRoomNumber() + " is already booked for these overlapping dates.");
                }
            }
        }

        if (checkIn.isBefore(LocalDate.now()))
            throw new InvalidDateRangeException("Check-in date cannot be in the past.");
        if (!checkOut.isAfter(checkIn))
            throw new InvalidDateRangeException("Check-out date must be after check-in date.");
            
        Reservation reservation = new Reservation(this, room, checkIn, checkOut);
        room.setAvailable(false);
        HotelDatabase.reservations.add(reservation);
        System.out.println("Reservation made successfully!");
        return reservation;
    }

    public void viewReservations() {
        System.out.println("Your Reservations:");
        for (Reservation r : HotelDatabase.reservations)
            if (r.getGuest().getUsername().equals(this.getUsername()))
                System.out.println("  Room: " + r.getRoom().getRoomNumber() + " | Status: " + r.getStatus());
    }

    public void cancelReservation(String roomNumber) {
        for (Reservation r : HotelDatabase.reservations)
            if (r.getGuest().getUsername().equals(this.getUsername())
                    && r.getRoom().getRoomNumber().equals(roomNumber)) {
                r.cancel();
                System.out.println("Reservation cancelled.");
                return;
            }
        System.out.println("Reservation not found.");
    }

    // Getters
    public String getRoomPreferences() { return roomPreferences; }
    public void setBalance(double balance) {
        if (balance < 0)
            throw new IllegalArgumentException("Balance cannot be negative.");
        this.balance = balance;
    }
    public void setRoomPreferences(String roomPreferences) { this.roomPreferences = roomPreferences; }

    @Override
    public String toString() {
        return "Guest{username=" + getUsername() + ", balance=" + balance + "}";
    }
}
