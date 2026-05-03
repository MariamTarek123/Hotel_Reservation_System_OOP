package models;
import interfaces.Manageable;
import enums.genders;
import enums.role;
import java.time.LocalDate;
import database.HotelDatabase;

/**
 * Represents an Administrator in the hotel reservation system.
 * Inherits from Staff and implements Manageable to provide full CRUD
 * operations on rooms, amenities, and room types.
 */
public class Admin extends Staff implements Manageable {

    public Admin(String username, String password, LocalDate dateOfBirth, String address, genders gender, int workingHours) {
        super(username, password, dateOfBirth, address, gender, role.Admin, workingHours);
    }



    /*
        Each method takes a general Object as a parameter, checks its type, and performs the appropriate CRUD operation.
        It uses instanceof to determine the type of object (Room, Amenity, or RoomType) and interacts with the corresponding list in HotelDatabase.
    */

    @Override
    public void create(Object obj) {
        if (obj instanceof Room) {
            HotelDatabase.rooms.add((Room) obj);
            System.out.println("Room successfully created: " + obj);
        } else if (obj instanceof Amenity) {
            HotelDatabase.amenities.add((Amenity) obj);
            System.out.println("Amenity created: " + obj);
        } else if (obj instanceof RoomType) {
            HotelDatabase.roomTypes.add((RoomType) obj);
            System.out.println("Room Type created: " + obj);
        } else {
            System.out.println("Item cannot be created by Admin.");
        }
    }

    @Override
    public void read(Object obj) {
        if (obj instanceof Room) {
            System.out.println("Room details: " + obj);
        } else if (obj instanceof Amenity) {
            System.out.println("Amenity details: " + obj);
        } else if (obj instanceof RoomType) {
            System.out.println("Room Type details: " + obj);
        } else {
            System.out.println("Unknown item details.");
        }
    }

    @Override
    public void update(Object obj) {
        if (obj instanceof Room) {
            int index = HotelDatabase.rooms.indexOf((Room) obj); //find the index of the room in the list
            //if the .index is not -1, it means the room exists in the list and we can update it
            if (index != -1) {
                HotelDatabase.rooms.set(index, (Room) obj);
                System.out.println("Room successfully updated: " + obj);
            } else {
                System.out.println("Room not found to update.");
            }
        } else if (obj instanceof Amenity) {
            int index = HotelDatabase.amenities.indexOf((Amenity) obj);
            if (index != -1) {
                HotelDatabase.amenities.set(index, (Amenity) obj);
                System.out.println("Amenity successfully updated: " + obj);
            } else {
                System.out.println("Amenity not found to update.");
            }
        } else if (obj instanceof RoomType) {
            int index = HotelDatabase.roomTypes.indexOf((RoomType) obj);
            if (index != -1) {
                HotelDatabase.roomTypes.set(index, (RoomType) obj);
                System.out.println("Room Type successfully updated: " + obj);
            } else {
                System.out.println("Room Type not found to update.");
            }
        } else {
             System.out.println("Update logic for this object is not defined.");
        }
    }


    @Override
    public void delete(Object obj) {
        if (obj instanceof Room) {
            if (HotelDatabase.rooms.remove((Room) obj)) {
                System.out.println("Room successfully deleted: " + obj);
            } else {
                System.out.println("Room not found to delete.");
            }
        } else if (obj instanceof Amenity) {
            if (HotelDatabase.amenities.remove((Amenity) obj)) {
                System.out.println("Amenity successfully deleted.");
            }
        } else if (obj instanceof RoomType) {
            if (HotelDatabase.roomTypes.remove((RoomType) obj)) {
                System.out.println("Room Type successfully deleted.");
            }
        } else {
            System.out.println("Item cannot be deleted by Admin.");
        }
    }


//    //  CRUD operations
//
//     //Adds a new room to the system.
//     //@param room the room to add
//    public void createRoom(Room room) {
//        HotelDatabase.rooms.add(room);
//        System.out.println("Room successfully created: " + room);
//    }
//
//     // Updates an existing room in the system.
//     // @param room the room to update
//    public void updateRoom(Room room) {
//        int index = HotelDatabase.rooms.indexOf(room);
//        if (index != -1) {
//            HotelDatabase.rooms.set(index, room);
//            System.out.println("Room successfully updated: " + room);
//        } else {
//            System.out.println("Room not found to update.");
//        }
//    }
//
//
//     //Removes an existing room from the system.
//     // @param room the room to delete
//    public void deleteRoom(Room room) {
//        if (HotelDatabase.rooms.remove(room)) {
//            System.out.println("Room successfully deleted: " + room);
//        } else {
//            System.out.println("Room not found to delete.");
//        }
//    }
//
//
//     // Adds a new amenity to the system.
//     // @param amenity the amenity to create
//
//    public void createAmenity(Amenity amenity) {
//        HotelDatabase.amenities.add(amenity);
//        System.out.println("Amenity created: " + amenity);
//    }
//
//
//     // Adds a new room type to the system.
//     // @param roomType the room type to create
//    public void createRoomType(RoomType roomType) {
//        HotelDatabase.roomTypes.add(roomType);
//        System.out.println("Room Type created: " + roomType);
//    }

}
