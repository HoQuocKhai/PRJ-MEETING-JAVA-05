package model.dto;

import model.Enum.EquipmentStatus;

/**
 * DTO dùng khi lấy thông tin thiết bị KÈM SỐ LƯỢNG đã mượn trong 1 booking.
 *
 * Lý do tách khỏi Equipment entity:
 * - Equipment.quantity là tổng số thiết bị trong kho.
 * - borrowedQuantity chỉ có ý nghĩa khi JOIN với bảng booking_equipments.
 * - Tương tự cách BookingServiceDetail tách khỏi ServiceItem (Bug E đã fix).
 *
 * Dùng ở: BookingDAO.getEquipmentsByBookingId(), SupportStaffService, BookingService, ExportBillUtil
 */
public class BookingEquipmentDetail {
    private int equipmentId;
    private String equipmentName;
    private int borrowedQuantity;   // Số lượng mượn trong booking này (từ booking_equipments.quantity)
    private int availableInStock;   // Số lượng còn lại trong kho (từ equipments.available)
    private EquipmentStatus status;

    public BookingEquipmentDetail() {}

    public BookingEquipmentDetail(int equipmentId, String equipmentName,
                                  int borrowedQuantity, int availableInStock, EquipmentStatus status) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.borrowedQuantity = borrowedQuantity;
        this.availableInStock = availableInStock;
        this.status = status;
    }

    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public int getBorrowedQuantity() { return borrowedQuantity; }
    public void setBorrowedQuantity(int borrowedQuantity) { this.borrowedQuantity = borrowedQuantity; }

    public int getAvailableInStock() { return availableInStock; }
    public void setAvailableInStock(int availableInStock) { this.availableInStock = availableInStock; }

    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }
}
