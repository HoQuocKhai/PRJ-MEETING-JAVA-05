package service;

import dao.BookingDAO;
import dao.EquipmentDAO;
import dao.NotificationDAO;
import dao.ServiceItemDAO;
import model.Booking;
import model.BookingDetailEquipment;
import model.BookingDetailService;
import model.Room;
import model.ServiceItem;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final ServiceItemDAO serviceItemDAO = new ServiceItemDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    /**
     * Trả về danh sách phòng trống dựa trên validation về thời gian và số người yêu cầu
     */
    public List<Room> getAvailableRooms(LocalDateTime start, LocalDateTime end, int capacity) throws Exception {
        if (start.isBefore(LocalDateTime.now())) {
            throw new Exception("Lỗi: Thời gian bắt đầu (startTime) không được ở trong quá khứ.");
        }
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new Exception("Lỗi: Thời gian kết thúc (endTime) phải lớn hơn thời gian bắt đầu.");
        }

        // Gọi xuống DAO
        return bookingDAO.getAvailableRooms(Timestamp.valueOf(start), Timestamp.valueOf(end), capacity);
    }

    /**
     * Hàm lưu thông tin Booking. Thực hiện validate lần cuối trước khi đẩy vào DB.
     */
    public boolean createBooking(Booking booking, List<BookingDetailEquipment> eqList, List<BookingDetailService> svList) throws Exception {
        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new Exception("Lỗi: Thời gian bắt đầu (startTime) không được ở trong quá khứ.");
        }
        if (booking.getEndTime().isBefore(booking.getStartTime()) || booking.getEndTime().isEqual(booking.getStartTime())) {
            throw new Exception("Lỗi: Thời gian kết thúc (endTime) phải lớn hơn thời gian bắt đầu.");
        }

        // Push xuống BookingDAO thực hiện Transaction
        return bookingDAO.insertBooking(booking, eqList, svList);
    }

    /**
     * Hàm tính tổng chi phí dịch vụ (Dùng biểu giá từ DB tính nhân số lượng yêu cầu)
     */
    public double calculateTotalServiceCost(List<BookingDetailService> svList) throws Exception {
        double total = 0.0;
        if (svList == null || svList.isEmpty()) {
            return total;
        }

        for (BookingDetailService detail : svList) {
            ServiceItem service = serviceItemDAO.getServiceById(detail.getServiceId());
            if (service != null) {
                total += service.getPrice() * detail.getQuantity();
            }
        }
        return total;
    }

    public List<Booking> getPendingBookings() throws Exception {
        return bookingDAO.getPendingBookings();
    }

    public boolean approveBooking(int bookingId, int staffId) throws Exception {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new Exception("Lỗi: Không tìm thấy Booking ID!");
        }
        if (booking.getBookingStatus() != model.Enum.BookingStatus.PENDING) {
            throw new Exception("Lỗi: Booking hiện tại không ở trạng thái PENDING.");
        }

        // Double check: Verify room is still available!
        // We use capacity = 1 since we only care about overlap check if participants was not saved, but we have participants field.
        List<Room> availableRooms = bookingDAO.getAvailableRooms(
            Timestamp.valueOf(booking.getStartTime()),
            Timestamp.valueOf(booking.getEndTime()),
            booking.getParticipants() > 0 ? booking.getParticipants() : 1
        );

        boolean roomStillAvailable = availableRooms.stream().anyMatch(r -> r.getRoomId() == booking.getRoomId());
        if (!roomStillAvailable) {
            throw new Exception("Lỗi: Xung đột lịch. Phòng này đã được Duyệt cho một người khác trong cùng khung giờ.");
        }

        boolean success = bookingDAO.approveAndAssign(bookingId, staffId);
        if (success) {
            notificationDAO.insertNotification(booking.getUserId(), "CHÚC MỪNG: Lịch đặt phòng #" + bookingId + " của bạn đã được Admin phê duyệt!");
        }
        return success;
    }

    public boolean rejectBooking(int bookingId) throws Exception {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new Exception("Lỗi: Không tìm thấy Booking ID!");
        }
        if (booking.getBookingStatus() != model.Enum.BookingStatus.PENDING) {
            throw new Exception("Lỗi: Booking hiện tại không ở trạng thái PENDING.");
        }
        boolean success = bookingDAO.rejectBooking(bookingId);
        if (success) {
            notificationDAO.insertNotification(booking.getUserId(), "RẤT TIẾC: Lịch đặt phòng #" + bookingId + " của bạn đã BỊ TỪ CHỐI do lịch ban giám đốc hoặc trùng đột xuất.");
        }
        return success;
    }

    public List<Booking> getBookingHistory(int userId) throws Exception {
        return bookingDAO.getBookingHistoryByUserId(userId);
    }

    public boolean cancelBooking(int bookingId, int userId) throws Exception {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new Exception("Lỗi: Không tìm thấy Booking ID!");
        }
        if (booking.getUserId() != userId) {
            throw new Exception("Lỗi: Bạn không có quyền hủy Booking của người khác!");
        }
        if (booking.getBookingStatus() != model.Enum.BookingStatus.PENDING) {
            throw new Exception("Lỗi: Chỉ có thể hủy Booking khi đang ở trạng thái chờ duyệt (PENDING).");
        }
        
        return bookingDAO.cancelBooking(bookingId, userId);
    }

    public double calculateCompletedRevenue(int month, int year) throws Exception {
        return bookingDAO.calculateCompletedRevenue(month, year);
    }

    public void printRoomUsageStatistics() throws Exception {
        bookingDAO.printRoomUsageStatistics();
    }

    public boolean exportBill(int bookingId) throws Exception {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new Exception("Lỗi: Không tìm thấy Booking ID!");
        }
        if (booking.getPreparationStatus() != model.Enum.PreparationStatus.READY) {
            throw new Exception("Lỗi: Booking hiện tại chưa hoàn tất (Chưa READY). Không thể xuất hóa đơn!");
        }

        java.util.List<model.Equipment> eqList = bookingDAO.getEquipmentsByBookingId(bookingId);
        java.util.List<model.ServiceItem> svList = bookingDAO.getServicesByBookingId(bookingId);

        // Tính tổng tiền service
        double totalCost = 0;
        for (model.ServiceItem s : svList) {
            totalCost += s.getPrice() * s.getOrderQuantity();
        }

        return util.ExportBillUtil.exportBillToFile(booking, totalCost, eqList, svList);
    }
}
