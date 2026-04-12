package dao;

import model.Booking;
import model.BookingDetailEquipment;
import model.BookingDetailService;
import model.Room;
import model.Enum.BookingStatus;
import model.Enum.PreparationStatus;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO extends BaseDAO<Booking> {

    @Override
    protected Booking mapResultSetToObject(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("bookingId"));
        b.setUserId(rs.getInt("userId"));
        b.setRoomId(rs.getInt("roomId"));
        
        int supportAStaffId = rs.getInt("supportStaffId");
        if (!rs.wasNull()) {
            b.setSupportStaffId(supportAStaffId);
        } else {
            b.setSupportStaffId(null);
        }
        
        b.setStartTime(rs.getTimestamp("startTime").toLocalDateTime());
        b.setEndTime(rs.getTimestamp("endTime").toLocalDateTime());
        
        // Map BookingStatus
        String bStatusStr = rs.getString("bookingStatus");
        if (bStatusStr != null) {
            b.setBookingStatus(BookingStatus.valueOf(bStatusStr));
        }
        
        // Map PreparationStatus
        String pStatusStr = rs.getString("preparationStatus");
        if (pStatusStr != null) {
            b.setPreparationStatus(PreparationStatus.valueOf(pStatusStr));
        }

        b.setCreatedAt(rs.getTimestamp("createdAt"));
        
        // Nếu có xử lý participants lưu động, phải có logic thêm ở đây (hiện tại DB chưa có participants)
        // b.setParticipants(rs.getInt("participants"));

        return b;
    }

    /**
     * Hàm lấy danh sách phòng trống
     * @param start Thời gian dự kiến bắt đầu
     * @param end Thời gian dự kiến kết thúc
     * @param capacity Sức chứa tối thiểu yêu cầu
     * @return Danh sách các phòng trống thỏa mản
     */
    public List<Room> getAvailableRooms(Timestamp start, Timestamp end, int capacity) throws SQLException {
        List<Room> availableRooms = new ArrayList<>();
        // Logic SQL: Lấy các phòng có capacity thỏa mãn và không bị trùng lịch (không overlap).
        // Phép overlap: (b.startTime < request.end) AND (b.endTime > request.start)
        String sql = "SELECT * FROM rooms r " +
                "WHERE r.capacity >= ? " +
                "AND r.roomId NOT IN (" +
                "    SELECT b.roomId FROM bookings b " +
                "    WHERE b.bookingStatus IN ('PENDING', 'APPROVED') " +
                "    AND (b.startTime < ? AND b.endTime > ?)" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, capacity);
            pstmt.setTimestamp(2, end);
            pstmt.setTimestamp(3, start);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Room r = new Room();
                    r.setRoomId(rs.getInt("roomId"));
                    r.setRoomName(rs.getString("roomName"));
                    r.setCapacity(rs.getInt("capacity"));
                    r.setLocation(rs.getString("location"));
                    r.setFixedDevice(rs.getString("fixedDevice"));
                    availableRooms.add(r);
                }
            }
        }
        return availableRooms;
    }

    /**
     * Hàm lưu Booking sử dụng SQL Transaction.
     * Đảm bảo insert cả bookings và các detail (mượn thiết bị, mượn dịch vụ) cùng 1 lúc mà không bị rác dữ liệu.
     */
    public boolean insertBooking(Booking b, List<BookingDetailEquipment> eqList, List<BookingDetailService> svList) throws SQLException {
        Connection conn = null;
        PreparedStatement psBooking = null;
        PreparedStatement psEq = null;
        PreparedStatement psSv = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Insert thông tin chính vào bảng bookings
            String sqlBooking = "INSERT INTO bookings(userId, roomId, startTime, endTime, bookingStatus, preparationStatus) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            psBooking = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS);
            psBooking.setInt(1, b.getUserId());
            psBooking.setInt(2, b.getRoomId());
            psBooking.setTimestamp(3, Timestamp.valueOf(b.getStartTime()));
            psBooking.setTimestamp(4, Timestamp.valueOf(b.getEndTime()));
            psBooking.setString(5, b.getBookingStatus().name());
            psBooking.setString(6, b.getPreparationStatus().name());

            int affectedRows = psBooking.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Thêm Booking thất bại (không có row nào bị thay đổi).");
            }

            // Lấy ra Autoincrement bookingId do MySQL tự sinh
            rs = psBooking.getGeneratedKeys();
            int newBookingId = -1;
            if (rs.next()) {
                newBookingId = rs.getInt(1);
                b.setBookingId(newBookingId);
            } else {
                throw new SQLException("Tạo Booking thất bại, không truy xuất được ID auto_increment.");
            }

            // 2. Insert Thiết bị mượn (nếu có)
            if (eqList != null && !eqList.isEmpty()) {
                String sqlEq = "INSERT INTO booking_equipments(bookingId, equipmentId, quantity) VALUES (?, ?, ?)";
                psEq = conn.prepareStatement(sqlEq);
                for (BookingDetailEquipment eq : eqList) {
                    psEq.setInt(1, newBookingId);
                    psEq.setInt(2, eq.getEquipmentId());
                    psEq.setInt(3, eq.getQuantity());
                    psEq.addBatch();
                }
                psEq.executeBatch();
            }

            // 3. Insert Dịch vụ mượn (nếu có)
            if (svList != null && !svList.isEmpty()) {
                String sqlSv = "INSERT INTO booking_services(bookingId, serviceId, quantity) VALUES (?, ?, ?)";
                psSv = conn.prepareStatement(sqlSv);
                for (BookingDetailService sv : svList) {
                    psSv.setInt(1, newBookingId);
                    psSv.setInt(2, sv.getServiceId());
                    psSv.setInt(3, sv.getQuantity());
                    psSv.addBatch();
                }
                psSv.executeBatch();
            }

            conn.commit(); // Tất cả đều Ok, tiến hành Confirm thay đổi
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Hủy bỏ toàn bộ thao tác insert dở dang
                } catch (SQLException ex) {
                    System.err.println("Lỗi Rollback: " + ex.getMessage());
                }
            }
            throw e; // Ném lại lỗi ra Service để báo cho người dùng
        } finally {
            // Đóng các tài nguyên
            if (rs != null) rs.close();
            if (psBooking != null) psBooking.close();
            if (psEq != null) psEq.close();
            if (psSv != null) psSv.close();
            if (conn != null) {
                conn.setAutoCommit(true); // Reset về trạng thái mặc định trước khi trả pool
                conn.close();
            }
        }
    }

    public List<Booking> getPendingBookings() throws SQLException {
        return executeQuery("SELECT * FROM bookings WHERE bookingStatus = 'PENDING'");
    }

    public Booking getBookingById(int bookingId) throws SQLException {
        return executeQueryForSingleObject("SELECT * FROM bookings WHERE bookingId = ?", bookingId);
    }

    public boolean approveAndAssign(int bookingId, int staffId) throws SQLException {
        String sql = "UPDATE bookings SET bookingStatus = 'APPROVED', supportStaffId = ?, preparationStatus = 'PREPARING' WHERE bookingId = ?";
        return executeUpdate(sql, staffId, bookingId);
    }

    public boolean rejectBooking(int bookingId) throws SQLException {
        String sql = "UPDATE bookings SET bookingStatus = 'REJECTED' WHERE bookingId = ?";
        return executeUpdate(sql, bookingId);
    }

    public List<Booking> getBookingsBySupportStaff(int staffId) throws SQLException {
        // Chỉ lấy những booking chưa hoàn tất: trạng thái Booking là APPROVED 
        // và preparationStatus trong ('PENDING', 'PREPARING')
        // (Trong schema: PREPARING, MISSING_EQUIPMENT cũng được)
        String sql = "SELECT * FROM bookings WHERE supportStaffId = ? AND bookingStatus = 'APPROVED' AND preparationStatus IN ('PENDING', 'PREPARING', 'MISSING_EQUIPMENT')";
        return executeQuery(sql, staffId);
    }

    public boolean updatePreparationStatus(int bookingId, PreparationStatus newStatus) throws SQLException {
        String sql = "UPDATE bookings SET preparationStatus = ? WHERE bookingId = ?";
        return executeUpdate(sql, newStatus.name(), bookingId);
    }

    public List<model.Equipment> getEquipmentsByBookingId(int bookingId) throws SQLException {
        List<model.Equipment> list = new ArrayList<>();
        String sql = "SELECT e.equipmentId, e.equipmentName, be.quantity, e.available, e.status " +
                     "FROM booking_equipments be JOIN equipments e ON be.equipmentId = e.equipmentId " +
                     "WHERE be.bookingId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.Equipment eq = new model.Equipment();
                    eq.setEquipmentId(rs.getInt("equipmentId"));
                    eq.setEquipmentName(rs.getString("equipmentName"));
                    eq.setQuantity(rs.getInt("quantity")); // Mượn tạm trường quantity để lưu số lượng cần mượn
                    eq.setAvailable(rs.getInt("available"));
                    String st = rs.getString("status");
                    if (st != null) eq.setStatus(model.Enum.EquipmentStatus.valueOf(st));
                    list.add(eq);
                }
            }
        }
        return list;
    }

    public List<model.ServiceItem> getServicesByBookingId(int bookingId) throws SQLException {
        List<model.ServiceItem> list = new ArrayList<>();
        String sql = "SELECT s.serviceId, s.serviceName, s.unit, s.price, s.description, bs.quantity " +
                     "FROM booking_services bs JOIN services s ON bs.serviceId = s.serviceId " +
                     "WHERE bs.bookingId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.ServiceItem s = new model.ServiceItem();
                    s.setServiceId(rs.getInt("serviceId"));
                    s.setServiceName(rs.getString("serviceName"));
                    s.setUnit(rs.getString("unit"));
                    s.setPrice(rs.getDouble("price"));
                    s.setDescription(rs.getString("description"));
                    s.setOrderQuantity(rs.getInt("quantity")); // Dùng trường tạm DTO
                    
                    list.add(s);
                }
            }
        }
        return list;
    }

    public List<Booking> getBookingHistoryByUserId(int userId) throws SQLException {
        // Tận dụng BaseDAO map thay vì map tay dài dòng như User viết
        String sql = "SELECT * FROM bookings WHERE userId = ? ORDER BY createdAt DESC";
        return executeQuery(sql, userId);
    }

    public boolean cancelBooking(int bookingId, int userId) throws SQLException {
        // Chỉ hủy nếu trạng thái đang là PENDING
        String sql = "UPDATE bookings SET bookingStatus = 'CANCELED' WHERE bookingId = ? AND userId = ? AND bookingStatus = 'PENDING'";
        return executeUpdate(sql, bookingId, userId);
    }

    public double calculateCompletedRevenue(int month, int year) throws SQLException {
        String sql = "SELECT SUM(s.price * bs.quantity) as totalRevenue " +
                     "FROM bookings b " +
                     "JOIN booking_services bs ON b.bookingId = bs.bookingId " +
                     "JOIN services s ON bs.serviceId = s.serviceId " +
                     "WHERE b.preparationStatus = 'READY' " +
                     "AND MONTH(b.startTime) = ? AND YEAR(b.startTime) = ?";
                     
        double total = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("totalRevenue");
                }
            }
        }
        return total;
    }

    public void printRoomUsageStatistics() throws SQLException {
        String sql = "SELECT r.roomId, r.roomName, COUNT(b.bookingId) as frequency " +
                     "FROM rooms r " +
                     "LEFT JOIN bookings b ON r.roomId = b.roomId " +
                     "GROUP BY r.roomId, r.roomName " +
                     "ORDER BY frequency DESC";
        
        System.out.println("\n--- THỐNG KÊ TẦN SUẤT SỬ DỤNG PHÒNG ---");
        System.out.printf("%-10s | %-20s | %-15s\n", "Room ID", "Tên Phòng", "Số Lần Mượn");
        System.out.println("--------------------------------------------------");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                System.out.printf("%-10d | %-20s | %-15d\n", 
                        rs.getInt("roomId"), rs.getString("roomName"), rs.getInt("frequency"));
            }
        }
    }
}
