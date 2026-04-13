package service;

import model.Booking;
import model.BookingDetailEquipment;
import model.BookingDetailService;
import model.Room;
import model.dto.BookingServiceDetail;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface cho BookingService.
 * Áp dụng Dependency Inversion Principle (DIP).
 */
public interface IBookingService {
    List<Room> getAvailableRooms(LocalDateTime start, LocalDateTime end, int capacity) throws Exception;
    boolean createBooking(Booking booking, List<BookingDetailEquipment> eqList, List<BookingDetailService> svList) throws Exception;
    double calculateTotalServiceCost(List<BookingDetailService> svList) throws Exception;
    List<Booking> getPendingBookings() throws Exception;
    boolean approveBooking(int bookingId, int staffId) throws Exception;
    boolean rejectBooking(int bookingId) throws Exception;
    List<Booking> getBookingHistory(int userId) throws Exception;
    boolean cancelBooking(int bookingId, int userId) throws Exception;
    double calculateCompletedRevenue(int month, int year) throws Exception;
    Map<Room, Integer> getRoomUsageStatistics() throws Exception;
    boolean exportBill(int bookingId) throws Exception;
}
