package database;

import enums.genders;
import enums.paymentmethod;
import models.*;

import java.time.LocalDate;
import java.util.ArrayList;

public class HotelDatabase {
    public static ArrayList<Guest> guests = new ArrayList<>();
    public static ArrayList<Room> rooms = new ArrayList<>();
    public static ArrayList<Reservation> reservations = new ArrayList<>();
    public static ArrayList<Invoice> invoices = new ArrayList<>();
    public static ArrayList<RoomType> roomTypes = new ArrayList<>();
    public static ArrayList<Amenity> amenities = new ArrayList<>();
    public static ArrayList<Staff> staff = new ArrayList<>();

    public static void populate() {
        // Room Types
        RoomType single = new RoomType(1, "Single", 50.0);
        RoomType double_ = new RoomType(2, "Double", 90.0);
        RoomType suite = new RoomType(3, "Suite", 200.0);
        roomTypes.add(single);
        roomTypes.add(double_);
        roomTypes.add(suite);

        // Amenities
        Amenity wifi = new Amenity(1, "WiFi", "Free wireless internet");
        Amenity tv = new Amenity(2, "TV", "Flat screen TV");
        Amenity minibar = new Amenity(3, "Mini-bar", "Stocked mini-bar");
        Amenity ac = new Amenity(4, "AC", "Air conditioning");
        amenities.add(wifi);
        amenities.add(tv);
        amenities.add(minibar);
        amenities.add(ac);

        // Rooms
        Room r1 = new Room(1, "101", 1, 50.0, single);
        r1.addAmenity(wifi);
        r1.addAmenity(tv);

        Room r2 = new Room(2, "102", 1, 90.0, double_);
        r2.addAmenity(wifi);
        r2.addAmenity(tv);
        r2.addAmenity(ac);

        Room r3 = new Room(3, "505", 2, 200.0, suite);
        r3.addAmenity(wifi);
        r3.addAmenity(tv);
        r3.addAmenity(minibar);
        r3.addAmenity(ac);

        Room r4 = new Room(4, "202", 2, 90.0, double_);
        r4.addAmenity(wifi);
        r4.addAmenity(ac);

        rooms.add(r1);
        rooms.add(r2);
        rooms.add(r3);
        rooms.add(r4);

        // Guests
        Guest g1 = new Guest("Farida", "ferry177", LocalDate.of(2000, 4, 1), "Cairo", genders.FEMALE, 1500.0, "Single");
        Guest g2 = new Guest("ibrahim", "Bebo123", LocalDate.of(1990, 7, 22), "Giza", genders.MALE, 300.0, "Single");
        Guest g3 = new Guest("carol", "kouki666", LocalDate.of(2000, 1, 15), "Alexandria", genders.FEMALE, 1000.0, "Suite");
        guests.add(g1);
        guests.add(g2);
        guests.add(g3);

        // Staff
        Admin admin = new Admin("Bohsen", "Bohsen123", LocalDate.of(1985, 1, 1), "Cairo", genders.MALE, 40);
        Receptionist rec = new Receptionist("shehebar", "shehebar123", LocalDate.of(1992, 5, 15), "Giza", genders.MALE, 35);
        staff.add(admin);
        staff.add(rec);

        // Reservations
        Reservation res1 = new Reservation(g1, r1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 5));
        r1.setAvailable(false);
        reservations.add(res1);

        Reservation res2 = new Reservation(g2, r2, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 12));
        reservations.add(res2);

        // Invoices
        Invoice inv1 = Invoice.generate(res1, res1.getNumberOfNights() * r1.getPricePerNight());
        inv1.markPaid(paymentmethod.CREDIT_CARD);
        invoices.add(inv1);
    }

    public static Guest findGuestByUsername(String username) {
        for (Guest g : guests)
            if (g.getUsername().equals(username))
                return g;
        return null;
    }

    public static ArrayList<Room> findAvailableRooms() {
        ArrayList<Room> available = new ArrayList<>();
        for (Room r : rooms)
            if (r.isAvailable())
                available.add(r);
        return available;
    }
}