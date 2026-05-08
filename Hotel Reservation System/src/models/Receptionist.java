package models;

import database.HotelDatabase;
import enums.genders;
import enums.role;
import enums.reservationstatus;
import java.time.LocalDate;
/**
 * Represents a receptionist in the hotel reservation system.
 * This class extends the Staff class.
 * Receptionists are responsible for managing guest check-ins and check-outs, as well as handling reservations.
 */

public class Receptionist extends Staff {

    public Receptionist(String username, String password, LocalDate dateOfBirth, String address, genders gender, int workingHours) {
        super(username, password, dateOfBirth, address, gender, role.receptionist, workingHours);
    }

    public void manageCheckIn(Reservation reservation) {
        if (reservation == null)
            throw new IllegalStateException("Reservation cannot be null.");

        LocalDate today = LocalDate.now();
        if (today.isBefore(reservation.getCheckInDate()))
            throw new IllegalStateException("Cannot check-in. Check-in date is " + reservation.getCheckInDate() + ".");

        if (reservation.getStatus() == reservationstatus.PENDING) {
            reservation.setStatus(reservationstatus.CONFIRMED);
            HotelDatabase.updateReservationStatus(reservation);
            System.out.println("Check-in successful: " + reservation);
        } else {
            throw new IllegalStateException("Cannot check-in. Status is already " + reservation.getStatus() + ".");
        }
    }

    public void manageCheckOut(Reservation reservation) {
        if (reservation == null)
            throw new IllegalStateException("Reservation cannot be null.");

        if (reservation.getStatus() == reservationstatus.CONFIRMED) {
            reservation.setStatus(reservationstatus.COMPLETED);
            reservation.getRoom().setAvailable(true);
            HotelDatabase.updateReservationStatus(reservation);
            HotelDatabase.updateRoomAvailability(reservation.getRoom());
            System.out.println("Check-out successful. Room freed.");
        } else {
            throw new IllegalStateException("Cannot check-out. Status is " + reservation.getStatus() + " (must be CONFIRMED).");
        }
    }

}