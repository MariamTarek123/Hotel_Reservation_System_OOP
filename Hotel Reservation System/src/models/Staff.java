package models;

import enums.role;
import enums.genders;
import database.HotelDatabase;

import java.time.LocalDate;
import java.util.List;
/**
 * Represents a staff member in the hotel reservation system.
 * This is an abstract class that extends Person and includes additional properties
 * and behaviors specific to staff members, such as their role (Admin or Receptionist)
 * and working hours. Staff members can view all guests, rooms, and reservations.
 * and manage them based on their role.

 */

public abstract class Staff extends Person {
    private role staffRole; //Admin, reseptionist
    private int workingHours;

    public Staff() {
        super();
    }

    public Staff(String username, String password, LocalDate dateOfBirth, String address, genders gender, role staffRole, int workingHours) {
        super(username, password, dateOfBirth, address, gender);
        this.staffRole = staffRole;
        this.workingHours = workingHours;
    }

    public role getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(role staffRole) {
        this.staffRole = staffRole;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(int workingHours) {
        this.workingHours = workingHours;
    }
    // Staff can view guests, rooms, and reservations

    // View all guests
    public void viewAllGuests() {
        List<Guest> guests = HotelDatabase.guests; //get the complete list of guests
        for (int i = 0; i < guests.size(); i++) {
            System.out.println(guests.get(i));
        }
    }

    // View all rooms
    public void viewAllRooms() {
        List<Room> rooms = HotelDatabase.rooms;
        for (int i = 0; i < rooms.size(); i++) {
            System.out.println(rooms.get(i));
        }
    }

    // View all reservations
    public void viewAllReservations() {
        List<Reservation> reservations = HotelDatabase.reservations;
        for (int i = 0; i < reservations.size(); i++) {
            System.out.println(reservations.get(i));
        }
    }
}
