package dao;

import model.Enum.EquipmentStatus;
import model.Equipment;
import model.Room;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAO {

    // ==========================================
    // HÀM HELPER: Dùng chung để map dữ liệu
    // ==========================================
    private Equipment mapResultSetToRoom(ResultSet rs) throws SQLException {
        Equipment r = new Equipment();
        r.setEquipmentId(rs.getInt("equipmentId"));
        r.setEquipmentName(rs.getString("equipmentName"));
        r.setQuantity(rs.getInt("quantity"));
        r.setAvailable(rs.getInt("available"));

        String statusStr = rs.getString("status");
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                r.setStatus(EquipmentStatus.valueOf(statusStr.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.err.println("Cảnh báo: Trạng thái không hợp lệ trong DB - " + statusStr);
            }
        }
        return r;
    }

    // ==========================================
    // CÁC HÀM CRUD CHÍNH
    // ==========================================

    // 1. Lấy danh sách tất cả phòng họp
    public List<Equipment> getAllEquipments() throws SQLException {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT * FROM equipments";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Gọi hàm helper thay vì viết lại 5 dòng code
                list.add(mapResultSetToRoom(rs));
            }
        }
        return list;
    }

    // 2. Thêm phòng họp mới
    public boolean insertRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (roomName, capacity, location, fixedDevice) VALUES (?, ?, ?, ?)";
        return executeUpdate(sql, room.getRoomName(), room.getCapacity(), room.getLocation(), room.getFixedDevice());
    }

    // 3. Cập nhật thông tin phòng họp
    public boolean updateRoom(Room room) throws SQLException {
        String sql = "UPDATE rooms SET roomName=?, capacity=?, location=?, fixedDevice=? WHERE roomId=?";
        return executeUpdate(sql, room.getRoomName(), room.getCapacity(), room.getLocation(), room.getFixedDevice(), room.getRoomId());
    }

    // 4. Xóa phòng họp
    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE roomId=?";
        return executeUpdate(sql, roomId);
    }

    // 5. Lấy thông tin phòng theo ID
    public Equipment getEquipmentById(int equipmentId) throws SQLException {
        String sql = "SELECT * FROM equipments WHERE equipmentId=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoom(rs);
                }
            }
        }
        return null;
    }

    // ==========================================
    // HÀM HELPER: Dùng chung cho Insert/Update/Delete (Nâng cao)
    // ==========================================
    private boolean executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Set các tham số tự động dựa trên số lượng arguments truyền vào
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate() > 0;
        }
    }
}
