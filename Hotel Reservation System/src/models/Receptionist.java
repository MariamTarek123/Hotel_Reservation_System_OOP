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
        if (reservation == null) {
            throw new IllegalStateException("Reservation cannot be null.");
        }

        // Prevent Check-in before the designated Check-in Date
        LocalDate today = LocalDate.now();
        if (today.isBefore(reservation.getCheckInDate())) {
            throw new IllegalStateException("Cannot check-in. The reservation date (" + reservation.getCheckInDate() + ") has not started yet.");
        }

        // Accept both PENDING and CONFIRMED — guest may have already paid (CONFIRMED)
        if (reservation.getStatus() == reservationstatus.PENDING
                || reservation.getStatus() == reservationstatus.CONFIRMED) {
            reservation.setStatus(reservationstatus.CONFIRMED);
            System.out.println("Check-in successful for reservation: " + reservation);
        } else {
            throw new IllegalStateException("Cannot check-in. Reservation is already " + reservation.getStatus() + ".");
        }
    }

    public void manageCheckOut(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalStateException("Reservation cannot be null.");
        }

        if (reservation.getStatus() == reservationstatus.CONFIRMED) {
            reservation.setStatus(reservationstatus.COMPLETED);

            // Make the room available for future bookings
            reservation.getRoom().setAvailable(true);

            // Remove the guest from the system as requested
            HotelDatabase.guests.remove(reservation.getGuest());

            System.out.println("Check-out successful for reservation. Room freed and Guest removed.");
        } else {
            throw new IllegalStateException("Cannot check-out. Invalid reservation status (must be CONFIRMED).");
        }
    }

}