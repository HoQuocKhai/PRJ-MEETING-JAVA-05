package service;

import dao.BookingDAO;
import model.Booking;
import model.dto.BookingEquipmentDetail;
import model.Enum.PreparationStatus;

import java.util.List;

public class SupportStaffService implements ISupportStaffService {
    private final BookingDAO bookingDAO = new BookingDAO();

    // Lấy danh sách nhiệm vụ của Staff
    public List<Booking> getTasksByStaffId(int staffId) throws Exception {
        return bookingDAO.getBookingsBySupportStaff(staffId);
    }

    // Cập nhật trạng thái
    public boolean updateTaskStatus(int bookingId, PreparationStatus newStatus) throws Exception {
        return bookingDAO.updatePreparationStatus(bookingId, newStatus);
    }

    public Booking getBookingInfo(int bookingId) throws Exception {
        return bookingDAO.getBookingById(bookingId);
    }

    public java.util.List<BookingEquipmentDetail> getTaskEquipments(int bookingId) throws Exception {
        return bookingDAO.getEquipmentsByBookingId(bookingId);
    }

    public java.util.List<model.dto.BookingServiceDetail> getTaskServices(int bookingId) throws Exception {
        return bookingDAO.getServicesByBookingId(bookingId);
    }
}
