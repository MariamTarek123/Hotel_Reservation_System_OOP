package models;

import java.util.ArrayList;

public class Room {
    private int roomId;
    private String roomNumber;
    private int floor;
    private boolean isAvailable;
    private double pricePerNight;
    private RoomType roomType;
    private ArrayList<Amenity> amenities;

    public Room(int roomId, String roomNumber, int floor, double pricePerNight, RoomType roomType) {
        if (roomNumber == null || roomNumber.isEmpty())
            throw new IllegalArgumentException("Room number cannot be empty.");
        if (pricePerNight < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
        if (roomType == null)
            throw new IllegalArgumentException("Room type cannot be null.");
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.pricePerNight = pricePerNight;
        this.roomType = roomType;
        this.isAvailable = true;
        this.amenities = new ArrayList<>();
    }

    public void addAmenity(Amenity amenity) {
        if (amenity == null)
            throw new IllegalArgumentException("Amenity cannot be null.");
        amenities.add(amenity);
    }

    public void removeAmenity(Amenity amenity) {
        amenities.remove(amenity);
    }

    // Getters
    public int getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public int getFloor() { return floor; }
    public boolean isAvailable() { return isAvailable; }
    public double getPricePerNight() { return pricePerNight; }
    public RoomType getRoomType() { return roomType; }
    public ArrayList<Amenity> getAmenities() { return amenities; }

    // Setters
    public void setAvailable(boolean available) { isAvailable = available; }
    public void setPricePerNight(double pricePerNight) {
        if (pricePerNight < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
        this.pricePerNight = pricePerNight;
    }
    public void setRoomType(RoomType roomType) {
        if (roomType == null)
            throw new IllegalArgumentException("Room type cannot be null.");
        this.roomType = roomType;
    }

    @Override
    public String toString() {
        return "Room{id=" + roomId + ", number=" + roomNumber +
                ", floor=" + floor + ", available=" + isAvailable +
                ", price=" + pricePerNight + ", type=" + roomType.getTypeName() + "}";
    }
}