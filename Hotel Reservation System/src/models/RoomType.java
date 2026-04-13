package models;


public class RoomType {
    private int typeId;
    private String typeName;
    private double basePrice;

    public RoomType(int typeId, String typeName, double basePrice) {
        if (typeName == null || typeName.isEmpty())
            throw new IllegalArgumentException("Type name cannot be empty.");
        if (basePrice < 0)
            throw new IllegalArgumentException("Base price cannot be negative.");
        this.typeId = typeId;
        this.typeName = typeName;
        this.basePrice = basePrice;
    }

    // Getters
    public int getTypeId() { return typeId; }
    public String getTypeName() { return typeName; }
    public double getBasePrice() { return basePrice; }

    // Setters
    public void setTypeName(String typeName) {
        if (typeName == null || typeName.isEmpty())
            throw new IllegalArgumentException("Type name cannot be empty.");
        this.typeName = typeName;
    }
    public void setBasePrice(double basePrice) {
        if (basePrice < 0)
            throw new IllegalArgumentException("Base price cannot be negative.");
        this.basePrice = basePrice;
    }

    @Override
    public String toString() {
        return "RoomType{id=" + typeId + ", name=" + typeName + ", basePrice=" + basePrice + "}";
    }
}
