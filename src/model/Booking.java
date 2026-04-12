package model;

import model.Enum.BookingStatus;
import model.Enum.PreparationStatus;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private int userId;
    private int roomId;
    private Integer supportStaffId; // Dùng Integer thay vì int vì có thể mang giá trị null (chưa phân công)
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int participants; // Thuộc tính này bạn yêu cầu nhưng trong MEETING.sql hiện chưa có cột này!
    private BookingStatus bookingStatus; 
    private PreparationStatus preparationStatus; 
    private Timestamp createdAt;

    public Booking() {}

    public Booking(int bookingId, int userId, int roomId, Integer supportStaffId, LocalDateTime startTime, LocalDateTime endTime, int participants, BookingStatus bookingStatus, PreparationStatus preparationStatus, Timestamp createdAt) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.roomId = roomId;
        this.supportStaffId = supportStaffId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participants = participants;
        this.bookingStatus = bookingStatus;
        this.preparationStatus = preparationStatus;
        this.createdAt = createdAt;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public Integer getSupportStaffId() { return supportStaffId; }
    public void setSupportStaffId(Integer supportStaffId) { this.supportStaffId = supportStaffId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getParticipants() { return participants; }
    public void setParticipants(int participants) { this.participants = participants; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public PreparationStatus getPreparationStatus() { return preparationStatus; }
    public void setPreparationStatus(PreparationStatus preparationStatus) { this.preparationStatus = preparationStatus; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
