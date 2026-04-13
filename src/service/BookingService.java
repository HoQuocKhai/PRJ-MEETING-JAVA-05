package service;

import dao.BookingDAO;
import dao.EquipmentDAO;
import dao.ServiceItemDAO;
import model.Booking;
import model.BookingDetailEquipment;
import model.BookingDetailService;
import model.Room;
import model.ServiceItem;
import model.dto.BookingEquipmentDetail;
import service.NotificationService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class BookingService implements IBookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final ServiceItemDAO serviceItemDAO = new ServiceItemDAO();
    // I-C fix: Dùng INotificationService thay vì NotificationDAO trực tiếp (SRP)
    private final INotificationService notificationService = new NotificationService();

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
     * I-B fix: 1 query thay vì N queries.
     * Xây dựng price map từ getAllServices() rồi tính tổng chính xác không loop DB.
     */
    public double calculateTotalServiceCost(List<BookingDetailService> svList) throws Exception {
        if (svList == null || svList.isEmpty()) return 0.0;

        java.util.Map<Integer, Double> priceMap = serviceItemDAO.getAllServices().stream()
                .collect(java.util.stream.Collectors.toMap(
                        ServiceItem::getServiceId,
                        ServiceItem::getPrice));

        return svList.stream()
                .mapToDouble(d -> priceMap.getOrDefault(d.getServiceId(), 0.0) * d.getQuantity())
                .sum();
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
            notificationService.insertNotification(booking.getUserId(),
                    "CHÚC MỮNG: Lịch đặt phòng #" + bookingId + " của bạn đã được Admin phê duyệt!");
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
            notificationService.insertNotification(booking.getUserId(),
                    "RẤT TIẼ: Lịch đặt phòng #" + bookingId + " của bạn đã BỊ TỪ CHỐI do lịch ban giám đốc hoặc trùng đột xuất.");
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

    public java.util.Map<Room, Integer> getRoomUsageStatistics() throws Exception {
        return bookingDAO.getRoomUsageStatistics();
    }

    public boolean exportBill(int bookingId) throws Exception {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new Exception("Lỗi: Không tìm thấy Booking ID!");
        }
        if (booking.getPreparationStatus() != model.Enum.PreparationStatus.READY) {
            throw new Exception("Lỗi: Booking hiện tại chưa hoàn tất (Chưa READY). Không thể xuất hóa đơn!");
        }

        List<BookingEquipmentDetail> eqList = bookingDAO.getEquipmentsByBookingId(bookingId);
        java.util.List<model.dto.BookingServiceDetail> svList = bookingDAO.getServicesByBookingId(bookingId);

        // Tính tổng tiền service
        double totalCost = svList.stream().mapToDouble(model.dto.BookingServiceDetail::getSubTotal).sum();

        return util.ExportBillUtil.exportBillToFile(booking, totalCost, eqList, svList);
    }
}
