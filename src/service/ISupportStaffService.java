package service;

import model.Booking;
import model.dto.BookingEquipmentDetail;
import model.dto.BookingServiceDetail;
import model.Enum.PreparationStatus;
import java.util.List;

/**
 * Interface cho SupportStaffService.
 * Áp dụng Dependency Inversion Principle (DIP).
 */
public interface ISupportStaffService {
    List<Booking> getTasksByStaffId(int staffId) throws Exception;
    Booking getBookingInfo(int bookingId) throws Exception;
    List<BookingEquipmentDetail> getTaskEquipments(int bookingId) throws Exception;
    List<BookingServiceDetail> getTaskServices(int bookingId) throws Exception;
    boolean updateTaskStatus(int bookingId, PreparationStatus status) throws Exception;
}
