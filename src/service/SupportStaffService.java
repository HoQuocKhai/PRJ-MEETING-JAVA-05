package service;

import dao.BookingDAO;
import model.Booking;
import model.Equipment;
import model.ServiceItem;
import model.Enum.PreparationStatus;

import java.util.List;

public class SupportStaffService {
    private final BookingDAO bookingDAO = new BookingDAO();

    // Lấy danh sách nhiệm vụ của Staff
    public List<Booking> getTasksByStaffId(int staffId) throws Exception {
        return bookingDAO.getBookingsBySupportStaff(staffId);
    }

    // Cập nhật trạng thái
    public boolean updateTaskStatus(int bookingId, PreparationStatus newStatus) throws Exception {
        return bookingDAO.updatePreparationStatus(bookingId, newStatus);
    }

    // In chi tiết nhiệm vụ (Bao gồm phòng, thiết bị, dịch vụ)
    public void printTaskDetails(int bookingId) throws Exception {
        Booking b = bookingDAO.getBookingById(bookingId);
        if (b == null) {
            System.out.println("=> Không tìm thấy Booking ID!");
            return;
        }

        System.out.println("\n--- CHI TIẾT NHIỆM VỤ (BOOKING ID: " + bookingId + ") ---");
        System.out.println("- Phòng (Room ID): " + b.getRoomId());
        System.out.println("- Thời gian bắt đầu: " + b.getStartTime());
        System.out.println("- Trạng thái hiện tại: " + b.getPreparationStatus());

        // Lấy danh sách thiết bị cần mang
        List<Equipment> equipments = bookingDAO.getEquipmentsByBookingId(bookingId);
        if (equipments.isEmpty()) {
            System.out.println("- Thiết bị cần chuẩn bị: KHÔNG CÓ");
        } else {
            System.out.println("- Thiết bị cần chuẩn bị:");
            for (Equipment eq : equipments) {
                // Ta map tạm quantity từ bảng mượn vào eq.getQuantity() lúc ở DAO.
                System.out.println("   + " + eq.getEquipmentName() + " (Số lượng: " + eq.getQuantity() + ")");
            }
        }

        // Lấy danh sách dịch vụ đi kèm
        List<ServiceItem> services = bookingDAO.getServicesByBookingId(bookingId);
        if (services.isEmpty()) {
            System.out.println("- Dịch vụ kèm theo: KHÔNG CÓ");
        } else {
            System.out.println("- Dịch vụ kèm theo:");
            for (ServiceItem s : services) {
                System.out.println("   + " + s.getServiceName() + " (Số lượng yêu cầu: " + s.getOrderQuantity() + " " + s.getUnit() + ")");
            }
        }
        System.out.println("----------------------------------------------");
    }
}
