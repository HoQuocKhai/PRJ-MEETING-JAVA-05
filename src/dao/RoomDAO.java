package dao;

import model.Room;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    // 1. Lấy danh sách tất cả phòng họp
    public List<Room> getAllRooms() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Room r = new Room();
                r.setRoomId(rs.getInt("roomId"));
                r.setRoomName(rs.getString("roomName"));
                r.setCapacity(rs.getInt("capacity"));
                r.setLocation(rs.getString("location"));
                r.setFixedDevice(rs.getString("fixedDevice"));
                list.add(r);
            }
        }
        return list;
    }

    // 2. Thêm phòng họp mới
    public boolean insertRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (roomName, capacity, location, fixedDevice) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomName());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getLocation());
            ps.setString(4, room.getFixedDevice());
            return ps.executeUpdate() > 0;
        }
    }

    // 3. Cập nhật thông tin phòng họp
    public boolean updateRoom(Room room) throws SQLException {
        String sql = "UPDATE rooms SET roomName=?, capacity=?, location=?, fixedDevice=? WHERE roomId=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomName());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getLocation());
            ps.setString(4, room.getFixedDevice());
            ps.setInt(5, room.getRoomId());
            return ps.executeUpdate() > 0;
        }
    }

    // 4. Xóa phòng họp
    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE roomId=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    // 5. Lấy thông tin phòng theo ID
    public Room getRoomById(int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE roomId=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Room r = new Room();
                    r.setRoomId(rs.getInt("roomId"));
                    r.setRoomName(rs.getString("roomName"));
                    r.setCapacity(rs.getInt("capacity"));
                    r.setLocation(rs.getString("location"));
                    r.setFixedDevice(rs.getString("fixedDevice"));
                    return r;
                }
            }
        }
        return null;
    }
}