package models;

import enums.reservationstatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents a hotel room reservation.
 * Links a Guest with a specific Room for a requested timeframe (check-in to check-out dates),
 * and tracks the lifecycle status of the booking.
 */
public class Reservation {
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private reservationstatus status;

    //no args constructor
    public Reservation() {
        this.status = reservationstatus.PENDING;
    }
    //parameterized constructor
    public Reservation(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this();
        setGuest(guest);
        setRoom(room);
        setCheckInDate(checkInDate);
        setCheckOutDate(checkOutDate);
        this.status = reservationstatus.PENDING;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("Guest cannot be null.");
        }
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        this.room = room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        if (checkInDate == null) {
            throw new IllegalArgumentException("Check-in date cannot be null.");
        }
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        if (checkOutDate == null) {
            throw new IllegalArgumentException("Check-out date cannot be null.");
        }
        this.checkOutDate = checkOutDate;
    }

    public reservationstatus getStatus() {
        return status;
    }

    public void setStatus(reservationstatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Reservation status cannot be null.");
        }
        this.status = status;
    }

    public void cancel() {
        this.status = reservationstatus.CANCELLED;
    }

    public void complete() {
        this.status = reservationstatus.COMPLETED;
    }

    public boolean isActive() {
        return status == reservationstatus.PENDING || status == reservationstatus.CONFIRMED;
    }

    // Check if this reservation overlaps with another reservation's dates
    public boolean overlaps(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || checkInDate == null || checkOutDate == null) {
            return false;
        }
        return checkIn.isBefore(checkOutDate) && checkOut.isAfter(checkInDate);
    }

    public long getNumberOfNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "guest=" + (guest == null ? "null" : guest.getUsername()) +
                ", room=" + room +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", status=" + status +
                '}';
    }
/*
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(guest, that.guest) && Objects.equals(room, that.room) && Objects.equals(checkInDate, that.checkInDate) && Objects.equals(checkOutDate, that.checkOutDate);
    }
*/

/*
    @Override
    public int hashCode() {
        return Objects.hash(guest, room, checkInDate, checkOutDate);
    }
 */
}
