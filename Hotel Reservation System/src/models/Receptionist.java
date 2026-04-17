package models;

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
        if (reservation != null && reservation.getStatus() == reservationstatus.PENDING) {
            reservation.setStatus(reservationstatus.CONFIRMED);
            System.out.println("Check-in successful for reservation: " + reservation);
        } else {
            System.out.println("Cannot check-in. Invalid reservation or status.");
        }
    }

    public void manageCheckOut(Reservation reservation) {
        if (reservation != null && reservation.getStatus() == reservationstatus.CONFIRMED) {
            reservation.setStatus(reservationstatus.COMPLETED);
            System.out.println("Check-out successful for reservation: " + reservation);
        } else {
            System.out.println("Cannot check-out. Invalid reservation or status.");
        }
    }

}