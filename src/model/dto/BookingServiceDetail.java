package model.dto;

/**
 * DTO (Data Transfer Object) dùng khi cần lấy thông tin dịch vụ KÈM SỐ LƯỢNG đã đặt.
 *
 * Lý do tách khỏi ServiceItem:
 * - ServiceItem là Entity ánh xạ bảng `services` (không có quantity).
 * - orderedQuantity chỉ có ý nghĩa khi JOIN với bảng `booking_services`.
 * - Đặt trong Entity làm ô nhiễm Model thuần túy (không tuân thủ SRP).
 *
 * Dùng ở: BookingDAO.getServicesByBookingId(), SupportStaffService, BookingService.exportBill()
 */
public class BookingServiceDetail {
    private int serviceId;
    private String serviceName;
    private String unit;
    private double price;
    private String description;
    private int orderedQuantity;

    public BookingServiceDetail() {}

    public BookingServiceDetail(int serviceId, String serviceName, String unit,
                                double price, String description, int orderedQuantity) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.unit = unit;
        this.price = price;
        this.description = description;
        this.orderedQuantity = orderedQuantity;
    }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrderedQuantity() { return orderedQuantity; }
    public void setOrderedQuantity(int orderedQuantity) { this.orderedQuantity = orderedQuantity; }

    /** Tính thành tiền của dòng này */
    public double getSubTotal() { return price * orderedQuantity; }
}
