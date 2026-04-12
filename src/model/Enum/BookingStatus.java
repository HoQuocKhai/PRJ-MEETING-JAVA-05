package model.Enum;

public enum BookingStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PREPARING,
    READY,
    CANCELED,
    MISSING_EQUIPMENT // Được thêm vào dựa trên preparationStatus của database
}
