package database;

import enums.genders;
import enums.paymentmethod;
import enums.reservationstatus;
import models.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class HotelDatabase {
    public static ArrayList<Guest>       guests       = new ArrayList<>();
    public static ArrayList<Room>        rooms        = new ArrayList<>();
    public static ArrayList<Reservation> reservations = new ArrayList<>();
    public static ArrayList<Invoice>     invoices     = new ArrayList<>();
    public static ArrayList<RoomType>    roomTypes    = new ArrayList<>();
    public static ArrayList<Amenity>     amenities    = new ArrayList<>();
    public static ArrayList<Staff>       staff        = new ArrayList<>();

    // ── INITIALISE ──────────────────────────────────────────────
    public static void populate() {
        createTables();
        loadAll();
        if (rooms.isEmpty()) insertDummyData();
    }

    // ── CREATE TABLES ────────────────────────────────────────────
    private static void createTables() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS room_types (
                    typeId    INTEGER PRIMARY KEY,
                    typeName  TEXT NOT NULL,
                    basePrice REAL NOT NULL
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS amenities (
                    amenityId   INTEGER PRIMARY KEY,
                    name        TEXT NOT NULL,
                    description TEXT
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    roomId        INTEGER PRIMARY KEY,
                    roomNumber    TEXT NOT NULL,
                    floor         INTEGER,
                    isAvailable   INTEGER DEFAULT 1,
                    pricePerNight REAL,
                    roomTypeId    INTEGER,
                    FOREIGN KEY (roomTypeId) REFERENCES room_types(typeId)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS room_amenities (
                    roomId    INTEGER,
                    amenityId INTEGER,
                    PRIMARY KEY (roomId, amenityId)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS guests (
                    username        TEXT PRIMARY KEY,
                    password        TEXT NOT NULL,
                    dateOfBirth     TEXT,
                    address         TEXT,
                    gender          TEXT,
                    balance         REAL DEFAULT 0,
                    roomPreferences TEXT
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS staff (
                    username     TEXT PRIMARY KEY,
                    password     TEXT NOT NULL,
                    dateOfBirth  TEXT,
                    address      TEXT,
                    gender       TEXT,
                    role         TEXT,
                    workingHours INTEGER
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    guestUsername TEXT,
                    roomId        INTEGER,
                    checkInDate   TEXT,
                    checkOutDate  TEXT,
                    status        TEXT,
                    FOREIGN KEY (guestUsername) REFERENCES guests(username),
                    FOREIGN KEY (roomId)        REFERENCES rooms(roomId)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS invoices (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    reservationId INTEGER,
                    amount        REAL,
                    paymentMethod TEXT,
                    paid          INTEGER DEFAULT 0,
                    FOREIGN KEY (reservationId) REFERENCES reservations(id)
                )""");

            System.out.println("Tables created.");
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }

    // ── LOAD ALL DATA FROM DB INTO ARRAYLISTS ────────────────────
    public static void loadAll() {
        guests.clear(); rooms.clear(); reservations.clear();
        invoices.clear(); roomTypes.clear(); amenities.clear(); staff.clear();

        loadRoomTypes();
        loadAmenities();
        loadRooms();
        loadGuests();
        loadStaff();
        loadReservations();
    }

    private static void loadRoomTypes() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM room_types")) {
            while (rs.next())
                roomTypes.add(new RoomType(rs.getInt("typeId"),
                        rs.getString("typeName"), rs.getDouble("basePrice")));
        } catch (SQLException e) { System.out.println("loadRoomTypes: " + e.getMessage()); }
    }

    private static void loadAmenities() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM amenities")) {
            while (rs.next())
                amenities.add(new Amenity(rs.getInt("amenityId"),
                        rs.getString("name"), rs.getString("description")));
        } catch (SQLException e) { System.out.println("loadAmenities: " + e.getMessage()); }
    }

    private static void loadRooms() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {
            while (rs.next()) {
                int roomTypeId = rs.getInt("roomTypeId");
                RoomType rt = roomTypes.stream()
                        .filter(r -> r.getTypeId() == roomTypeId)
                        .findFirst().orElse(null);
                Room room = new Room(rs.getInt("roomId"), rs.getString("roomNumber"),
                        rs.getInt("floor"), rs.getDouble("pricePerNight"), rt);
                room.setAvailable(rs.getInt("isAvailable") == 1);

                // load amenities for this room
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT a.* FROM amenities a JOIN room_amenities ra ON a.amenityId = ra.amenityId WHERE ra.roomId = ?")) {
                    ps.setInt(1, room.getRoomId());
                    ResultSet ars = ps.executeQuery();
                    while (ars.next())
                        room.addAmenity(new Amenity(ars.getInt("amenityId"),
                                ars.getString("name"), ars.getString("description")));
                }
                rooms.add(room);
            }
        } catch (SQLException e) { System.out.println("loadRooms: " + e.getMessage()); }
    }

    private static void loadGuests() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM guests")) {
            while (rs.next())
                guests.add(new Guest(
                        rs.getString("username"), rs.getString("password"),
                        LocalDate.parse(rs.getString("dateOfBirth")),
                        rs.getString("address"),
                        genders.valueOf(rs.getString("gender")),
                        rs.getDouble("balance"), rs.getString("roomPreferences")));
        } catch (SQLException e) { System.out.println("loadGuests: " + e.getMessage()); }
    }

    public static void saveStaff(Staff s) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO staff VALUES (?,?,?,?,?,?,?)")) {
            ps.setString(1, s.getUsername());
            ps.setString(2, s.getPassword());
            ps.setString(3, s.getDateOfBirth().toString());
            ps.setString(4, s.getAddress());
            ps.setString(5, s.getGender().toString());
            ps.setString(6, s.getStaffRole().toString());
            ps.setInt(7, s.getWorkingHours());
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println("saveStaff: " + e.getMessage()); }
    }

    private static void loadStaff() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM staff")) {
            while (rs.next()) {
                String role = rs.getString("role");
                if (role.equals("ADMIN"))
                    staff.add(new Admin(rs.getString("username"), rs.getString("password"),
                            LocalDate.parse(rs.getString("dateOfBirth")),
                            rs.getString("address"),
                            genders.valueOf(rs.getString("gender")),
                            rs.getInt("workingHours")));
                else
                    staff.add(new Receptionist(rs.getString("username"), rs.getString("password"),
                            LocalDate.parse(rs.getString("dateOfBirth")),
                            rs.getString("address"),
                            genders.valueOf(rs.getString("gender")),
                            rs.getInt("workingHours")));
            }
        } catch (SQLException e) { System.out.println("loadStaff: " + e.getMessage()); }
    }

    private static void loadReservations() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM reservations")) {
            while (rs.next()) {
                Guest g = findGuestByUsername(rs.getString("guestUsername"));
                int roomId = rs.getInt("roomId");
                Room r = rooms.stream().filter(room -> room.getRoomId() == roomId)
                        .findFirst().orElse(null);
                if (g != null && r != null) {
                    Reservation res = new Reservation(g, r,
                            LocalDate.parse(rs.getString("checkInDate")),
                            LocalDate.parse(rs.getString("checkOutDate")));
                    res.setStatus(reservationstatus.valueOf(rs.getString("status")));
                    reservations.add(res);
                }
            }
        } catch (SQLException e) { System.out.println("loadReservations: " + e.getMessage()); }
    }

    // ── INSERT DUMMY DATA (first run only) ───────────────────────
    private static void insertDummyData() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Room Types
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO room_types VALUES (?,?,?)");
            Object[][] rts = {{1,"Single",50.0},{2,"Double",90.0},{3,"Suite",200.0}};
            for (Object[] rt : rts) {
                ps.setInt(1,(int)rt[0]); ps.setString(2,(String)rt[1]); ps.setDouble(3,(double)rt[2]);
                ps.executeUpdate();
            }

            // Amenities
            ps = conn.prepareStatement("INSERT INTO amenities VALUES (?,?,?)");
            Object[][] ams = {{1,"WiFi","Free wireless internet"},{2,"TV","Flat screen TV"},
                    {3,"Mini-bar","Stocked mini-bar"},{4,"AC","Air conditioning"}};
            for (Object[] a : ams) {
                ps.setInt(1,(int)a[0]); ps.setString(2,(String)a[1]); ps.setString(3,(String)a[2]);
                ps.executeUpdate();
            }

            // Rooms
            ps = conn.prepareStatement(
                    "INSERT INTO rooms(roomId,roomNumber,floor,isAvailable,pricePerNight,roomTypeId) VALUES (?,?,?,?,?,?)");
            Object[][] rm = {{1,"101",1,1,50.0,1},{2,"102",1,1,90.0,2},
                    {3,"505",2,1,200.0,3},{4,"202",2,1,90.0,2},
                    {5,"103",1,1,50.0,1},{6,"104",1,1,50.0,1},
                    {7,"203",2,1,90.0,2},{8,"301",3,1,150.0,3},
                    {9,"302",3,1,200.0,3}};
            for (Object[] r : rm) {
                ps.setInt(1,(int)r[0]); ps.setString(2,(String)r[1]); ps.setInt(3,(int)r[2]);
                ps.setInt(4,(int)r[3]); ps.setDouble(5,(double)r[4]); ps.setInt(6,(int)r[5]);
                ps.executeUpdate();
            }

            // Room Amenities
            ps = conn.prepareStatement("INSERT INTO room_amenities VALUES (?,?)");
            int[][] ra = {{1,1},{1,2},{2,1},{2,2},{2,4},{3,1},{3,2},{3,3},{3,4},{4,1},{4,4},
                          {5,1},{5,2},{6,1},{6,2},{7,1},{7,2},{7,4},{8,1},{8,2},{8,3},{9,1},{9,3},{9,4}};
            for (int[] r : ra) {
                ps.setInt(1,r[0]); ps.setInt(2,r[1]); ps.executeUpdate();
            }

            // Guests
            ps = conn.prepareStatement(
                    "INSERT INTO guests VALUES (?,?,?,?,?,?,?)");
            Object[][] gs = {
                    {"Mariam","Mario123","2006-10-08","Cairo","FEMALE",15000.0,"Suite"},
                    {"Ibrahim","Bebo123","1990-07-22","Giza","MALE",700.0,"Single"},
                    {"carol","kouki666","2000-01-15","Alexandria","FEMALE",1000.0,"Suite"}
            };
            for (Object[] g : gs) {
                ps.setString(1,(String)g[0]); ps.setString(2,(String)g[1]);
                ps.setString(3,(String)g[2]); ps.setString(4,(String)g[3]);
                ps.setString(5,(String)g[4]); ps.setDouble(6,(double)g[5]);
                ps.setString(7,(String)g[6]); ps.executeUpdate();
            }

            // Staff
            ps = conn.prepareStatement("INSERT INTO staff VALUES (?,?,?,?,?,?,?)");
            Object[][] st = {
                    {"Bohsen","Bohsen123","1985-01-01","Cairo","MALE","ADMIN",40},
                    {"shehebar","shehebar123","1998-05-15","Giza","MALE","RECEPTIONIST",35}
            };
            for (Object[] s : st) {
                ps.setString(1,(String)s[0]); ps.setString(2,(String)s[1]);
                ps.setString(3,(String)s[2]); ps.setString(4,(String)s[3]);
                ps.setString(5,(String)s[4]); ps.setString(6,(String)s[5]);
                ps.setInt(7,(int)s[6]); ps.executeUpdate();
            }

            System.out.println("Dummy data inserted.");
            loadAll();

        } catch (SQLException e) { System.out.println("insertDummyData: " + e.getMessage()); }
    }

    // ── SAVE METHODS (call these when data changes) ──────────────
    public static void saveGuest(Guest g) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO guests VALUES (?,?,?,?,?,?,?)")) {
            ps.setString(1, g.getUsername()); ps.setString(2, g.getPassword());
            ps.setString(3, g.getDateOfBirth().toString()); ps.setString(4, g.getAddress());
            ps.setString(5, g.getGender().toString()); ps.setDouble(6, g.getBalance());
            ps.setString(7, g.getRoomPreferences()); ps.executeUpdate();
        } catch (SQLException e) { System.out.println("saveGuest: " + e.getMessage()); }
    }

    public static void saveReservation(Reservation r) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO reservations(guestUsername,roomId,checkInDate,checkOutDate,status) VALUES (?,?,?,?,?)")) {
            ps.setString(1, r.getGuest().getUsername());
            ps.setInt(2, r.getRoom().getRoomId());
            ps.setString(3, r.getCheckInDate().toString());
            ps.setString(4, r.getCheckOutDate().toString());
            ps.setString(5, r.getStatus().toString());
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println("saveReservation: " + e.getMessage()); }
    }

    public static void updateRoomAvailability(Room r) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE rooms SET isAvailable=? WHERE roomId=?")) {
            ps.setInt(1, r.isAvailable() ? 1 : 0);
            ps.setInt(2, r.getRoomId());
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println("updateRoom: " + e.getMessage()); }
    }

    public static void updateReservationStatus(Reservation r) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE reservations SET status=? WHERE guestUsername=? AND roomId=?")) {
            ps.setString(1, r.getStatus().toString());
            ps.setString(2, r.getGuest().getUsername());
            ps.setInt(3, r.getRoom().getRoomId());
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println("updateReservationStatus: " + e.getMessage()); }
    }

    public static void updateGuestBalance(Guest g) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE guests SET balance=? WHERE username=?")) {
            ps.setDouble(1, g.getBalance());
            ps.setString(2, g.getUsername());
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println("updateGuestBalance: " + e.getMessage()); }
    }

    // ── FIND METHODS ─────────────────────────────────────────────
    public static Guest findGuestByUsername(String username) {
        for (Guest g : guests)
            if (g.getUsername().equals(username)) return g;
        return null;
    }

    public static ArrayList<Room> findAvailableRooms() {
        ArrayList<Room> available = new ArrayList<>();
        for (Room r : rooms)
            if (r.isAvailable()) available.add(r);
        return available;
    }
}
