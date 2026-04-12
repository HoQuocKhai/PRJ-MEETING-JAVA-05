package service;

import dao.EquipmentDAO;
import model.Equipment;
import model.Enum.EquipmentStatus;
import java.util.List;

public class EquipmentService {
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();

    public void displayAllEquipments() {
        try {
            List<Equipment> equipments = equipmentDAO.getAllEquipments();
            if (equipments.isEmpty()) {
                System.out.println("Hiện tại chưa có thiết bị nào trong hệ thống.");
                return;
            }
            System.out.println("\n--- DANH SÁCH THIẾT BỊ ---");
            System.out.printf("%-5s | %-20s | %-10s | %-10s | %-15s\n", "ID", "Tên Thiết Bị", "Số lượng", "Sẵn có", "Trạng thái");
            System.out.println("-----------------------------------------------------------------------");
            for (Equipment e : equipments) {
                System.out.printf("%-5d | %-20s | %-10d | %-10d | %-15s\n",
                        e.getEquipmentId(), e.getEquipmentName(), e.getQuantity(), e.getAvailable(), 
                        (e.getStatus() != null ? e.getStatus().name() : "N/A"));
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy danh sách thiết bị: " + e.getMessage());
        }
    }

    public void addEquipment(String name, int quantity, int available, EquipmentStatus status) {
        try {
            Equipment eq = new Equipment(0, name, quantity, available, status);
            if (equipmentDAO.insertEquipment(eq)) {
                System.out.println("=> Thêm thiết bị thành công!");
            } else {
                System.out.println("=> Thêm thiết bị thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi thêm thiết bị: " + e.getMessage());
        }
    }

    public void updateEquipment(int equipmentId, String name, int quantity, int available, EquipmentStatus status) {
        try {
            Equipment existing = equipmentDAO.getEquipmentById(equipmentId);
            if (existing == null) {
                System.out.println("=> Không tìm thấy thiết bị có ID: " + equipmentId);
                return;
            }
            
            existing.setEquipmentName(name);
            existing.setQuantity(quantity);
            existing.setAvailable(available);
            existing.setStatus(status);
            
            if (equipmentDAO.updateEquipment(existing)) {
                System.out.println("=> Cập nhật thông tin thiết bị thành công!");
            } else {
                System.out.println("=> Cập nhật thông tin thiết bị thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi cập nhật thiết bị: " + e.getMessage());
        }
    }

    public void deleteEquipment(int equipmentId) {
        try {
            Equipment existing = equipmentDAO.getEquipmentById(equipmentId);
            if (existing == null) {
                System.out.println("=> Không tìm thấy thiết bị có ID: " + equipmentId);
                return;
            }
            
            if (equipmentDAO.deleteEquipment(equipmentId)) {
                System.out.println("=> Xóa thiết bị thành công!");
            } else {
                System.out.println("=> Xóa thiết bị thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi xóa thiết bị (có thể do đang được sử dụng): " + e.getMessage());
        }
    }
}
