package service;

import dao.EquipmentDAO;
import model.Equipment;
import model.Enum.EquipmentStatus;
import java.util.List;

public class EquipmentService implements IEquipmentService {
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();

    public List<Equipment> getAllEquipments() throws Exception {
        return equipmentDAO.getAllEquipments();
    }

    public boolean addEquipment(String name, int quantity, int available, EquipmentStatus status) throws Exception {
        Equipment eq = new Equipment(0, name, quantity, available, status);
        return equipmentDAO.insertEquipment(eq);
    }

    public boolean updateEquipment(int equipmentId, String name, int quantity, int available, EquipmentStatus status) throws Exception {
        Equipment existing = equipmentDAO.getEquipmentById(equipmentId);
        if (existing == null) {
            throw new Exception("Không tìm thấy thiết bị có ID: " + equipmentId);
        }
        
        existing.setEquipmentName(name);
        existing.setQuantity(quantity);
        existing.setAvailable(available);
        existing.setStatus(status);
        
        return equipmentDAO.updateEquipment(existing);
    }

    public boolean deleteEquipment(int equipmentId) throws Exception {
        Equipment existing = equipmentDAO.getEquipmentById(equipmentId);
        if (existing == null) {
            throw new Exception("Không tìm thấy thiết bị có ID: " + equipmentId);
        }
        return equipmentDAO.deleteEquipment(equipmentId);
    }
}
