package service;

import model.Equipment;
import model.Enum.EquipmentStatus;
import java.util.List;

/**
 * Interface cho EquipmentService.
 * Áp dụng Dependency Inversion Principle (DIP).
 */
public interface IEquipmentService {
    List<Equipment> getAllEquipments() throws Exception;
    boolean addEquipment(String name, int quantity, int available, EquipmentStatus status) throws Exception;
    boolean updateEquipment(int equipmentId, String name, int quantity, int available, EquipmentStatus status) throws Exception;
    boolean deleteEquipment(int equipmentId) throws Exception;
}
