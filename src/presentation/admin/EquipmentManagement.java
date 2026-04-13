package presentation.admin;

import model.Equipment;
import model.Enum.EquipmentStatus;
import service.IEquipmentService;
import service.EquipmentService;
import util.InputValidation;
import java.util.List;

/**
 * Quản lý CRUD Thiết bị di động.
 * Tách từ AdminConsole để tuân thủ Single Responsibility Principle.
 */
public class EquipmentManagement {
    private static final IEquipmentService equipmentService = new EquipmentService();

    public static void manage() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- QUẢN LÝ THIẾT BỊ DI ĐỘNG ---");
            System.out.println("1. Xem danh sách thiết bị di động");
            System.out.println("2. Thêm thiết bị mới");
            System.out.println("3. Sửa thông tin thiết bị");
            System.out.println("4. Xóa thiết bị di động");
            System.out.println("0. Quay lại menu chính");
            System.out.print("Lựa chọn: ");
            int choice = InputValidation.inputInt();

            switch (choice) {
                case 1 -> listEquipments();
                case 2 -> addEquipment();
                case 3 -> updateEquipment();
                case 4 -> deleteEquipment();
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void listEquipments() {
        try {
            List<Equipment> equipments = equipmentService.getAllEquipments();
            if (equipments.isEmpty()) {
                System.out.println("Hiện tại chưa có thiết bị nào trong hệ thống.");
            } else {
                System.out.println("\n--- DANH SÁCH THIẾT BỊ ---");
                System.out.printf("%-5s | %-20s | %-10s | %-10s | %-15s\n", "ID", "Tên Thiết Bị", "Số lượng", "Sẵn có", "Trạng thái");
                System.out.println("-----------------------------------------------------------------------");
                for (Equipment e : equipments) {
                    System.out.printf("%-5d | %-20s | %-10d | %-10d | %-15s\n",
                            e.getEquipmentId(), e.getEquipmentName(), e.getQuantity(), e.getAvailable(),
                            (e.getStatus() != null ? e.getStatus().name() : "N/A"));
                }
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi lấy danh sách thiết bị: " + e.getMessage());
        }
    }

    private static void addEquipment() {
        System.out.print("Nhập tên thiết bị: ");
        String name = InputValidation.inputString();
        System.out.print("Nhập số lượng: ");
        int quantity = InputValidation.inputInt();
        System.out.print("Nhập số lượng sẵn có: ");
        int available = InputValidation.inputInt();
        System.out.print("Nhập trạng thái (ACTIVE, MAINTENANCE, BROKEN): ");
        String statusStr = InputValidation.inputString();

        EquipmentStatus status;
        try {
            status = EquipmentStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Trạng thái không hợp lệ. Hủy thao tác.");
            return;
        }

        try {
            if (equipmentService.addEquipment(name, quantity, available, status)) {
                System.out.println("=> Thêm thiết bị thành công!");
            } else {
                System.out.println("=> Thêm thiết bị thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi thêm thiết bị: " + e.getMessage());
        }
    }

    private static void updateEquipment() {
        System.out.print("Nhập ID thiết bị cần sửa: ");
        int eqId = InputValidation.inputInt();
        System.out.print("Nhập tên thiết bị mới: ");
        String name = InputValidation.inputString();
        System.out.print("Nhập số lượng mới: ");
        int quantity = InputValidation.inputInt();
        System.out.print("Nhập số lượng sẵn có mới: ");
        int available = InputValidation.inputInt();
        System.out.print("Nhập trạng thái mới (ACTIVE, MAINTENANCE, BROKEN): ");
        String statusStr = InputValidation.inputString();

        EquipmentStatus status;
        try {
            status = EquipmentStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Trạng thái không hợp lệ. Hủy thao tác.");
            return;
        }

        try {
            if (equipmentService.updateEquipment(eqId, name, quantity, available, status)) {
                System.out.println("=> Cập nhật thông tin thiết bị thành công!");
            } else {
                System.out.println("=> Cập nhật thông tin thiết bị thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi cập nhật thiết bị: " + e.getMessage());
        }
    }

    private static void deleteEquipment() {
        System.out.print("Nhập ID thiết bị cần xóa: ");
        int eqId = InputValidation.inputInt();
        System.out.print("Bạn có chắc chắn muốn xóa thiết bị ID " + eqId + "? (Y/N): ");
        String confirm = InputValidation.inputString();
        if (confirm.equalsIgnoreCase("Y")) {
            try {
                if (equipmentService.deleteEquipment(eqId)) {
                    System.out.println("=> Xóa thiết bị thành công!");
                } else {
                    System.out.println("=> Xóa thiết bị thất bại.");
                }
            } catch (Exception e) {
                System.out.println("=> Lỗi khi xóa thiết bị (có thể do đang được sử dụng): " + e.getMessage());
            }
        } else {
            System.out.println("=> Đã hủy thao tác xóa.");
        }
    }
}
