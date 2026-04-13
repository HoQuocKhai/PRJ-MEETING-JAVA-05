package dao;

import model.Enum.EquipmentStatus;
import model.Equipment;
import model.Room;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EquipmentDAO extends BaseDAO<Equipment> {

    @Override
    protected Equipment mapResultSetToObject(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipmentId(rs.getInt("equipmentId"));
        e.setEquipmentName(rs.getString("equipmentName"));
        e.setQuantity(rs.getInt("quantity"));
        e.setAvailable(rs.getInt("available"));

        String statusStr = rs.getString("status");
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                e.setStatus(EquipmentStatus.valueOf(statusStr.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                    "Trạng thái thiết bị không hợp lệ trong DB: '" + statusStr + "'. " +
                    "Chỉ chấp nhận: ACTIVE, MAINTENANCE, BROKEN.", ex);
            }
        }
        return e;
    }

    public List<Equipment> getAllEquipments() throws SQLException {
        return executeQuery("SELECT * FROM equipments");
    }

    public Equipment getEquipmentById(int equipmentId) throws SQLException {
        return executeQueryForSingleObject("SELECT * FROM equipments WHERE equipmentId=?", equipmentId);
    }

    public boolean insertEquipment(Equipment equipment) throws SQLException {
        String sql = "INSERT INTO equipments (equipmentName, quantity, available, status) VALUES (?, ?, ?, ?)";
        return executeUpdate(sql, equipment.getEquipmentName(), equipment.getQuantity(), equipment.getAvailable(), equipment.getStatus().name());
    }

    public boolean updateEquipment(Equipment equipment) throws SQLException {
        String sql = "UPDATE equipments SET equipmentName=?, quantity=?, available=?, status=? WHERE equipmentId=?";
        return executeUpdate(sql, equipment.getEquipmentName(), equipment.getQuantity(), equipment.getAvailable(), equipment.getStatus().name(), equipment.getEquipmentId());
    }

    public boolean deleteEquipment(int equipmentId) throws SQLException {
        return executeUpdate("DELETE FROM equipments WHERE equipmentId=?", equipmentId);
    }
}