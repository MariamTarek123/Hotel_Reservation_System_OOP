package models;

public class Amenity {
    private int AmenityId;
    private String name;
    private String Description;

    public Amenity(int AmenityId, String name, String Description) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty.");
        this.AmenityId = AmenityId;
        this.name = name;
        this.Description = Description;

    }

    //Getters
    public int AmenityId() {
        return AmenityId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return Description;
    }

    // Setters

    public void setAmenityId(int AmenityId) {
        this.AmenityId = AmenityId;
    }

    public void setName(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty.");
        this.name = name;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }
    @Override
    public String toString() {
        return "Amenity{id=" + AmenityId + ", name=" + name + ", description=" + Description + "}";
    }
}