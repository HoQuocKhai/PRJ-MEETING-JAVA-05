package dao;

import model.Room;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class RoomDAO extends BaseDAO<Room> {

    // Ghi đè hàm map của class cha
    @Override
    protected Room mapResultSetToObject(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("roomId"));
        r.setRoomName(rs.getString("roomName"));
        r.setCapacity(rs.getInt("capacity"));
        r.setLocation(rs.getString("location"));
        r.setFixedDevice(rs.getString("fixedDevice"));
        return r;
    }

    public List<Room> getAllRooms() throws SQLException {
        return executeQuery("SELECT * FROM rooms");
    }

    public Room getRoomById(int roomId) throws SQLException {
        return executeQueryForSingleObject("SELECT * FROM rooms WHERE roomId=?", roomId);
    }

    public List<Room> getRoomByName(String keyword) throws SQLException {
        return executeQuery("SELECT * FROM rooms WHERE roomName LIKE ?", "%" + keyword + "%");
    }

    public boolean insertRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (roomName, capacity, location, fixedDevice) VALUES (?, ?, ?, ?)";
        return executeUpdate(sql, room.getRoomName(), room.getCapacity(), room.getLocation(), room.getFixedDevice());
    }

    public boolean updateRoom(Room room) throws SQLException {
        String sql = "UPDATE rooms SET roomName=?, capacity=?, location=?, fixedDevice=? WHERE roomId=?";
        return executeUpdate(sql, room.getRoomName(), room.getCapacity(), room.getLocation(), room.getFixedDevice(), room.getRoomId());
    }

    public boolean deleteRoom(int roomId) throws SQLException {
        return executeUpdate("DELETE FROM rooms WHERE roomId=?", roomId);
    }
}