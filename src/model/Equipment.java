package model;

import model.Enum.EquipmentStatus;

public class Equipment {
    private int equipmentId;
    private String equipmentName;
    private int quantity;
    private int available;
    private EquipmentStatus status;

    public Equipment(int equipmentId, String equipmentName, int quantity, int available, EquipmentStatus status) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.quantity = quantity;
        this.available = available;
        this.status = status;
    }

    public Equipment() {}

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }
}
