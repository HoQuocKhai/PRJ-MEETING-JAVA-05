package presentation.admin;

import model.ServiceItem;
import service.IServiceItemService;
import service.ServiceItemService;
import util.InputValidation;
import java.util.List;

/**
 * Quản lý CRUD Dịch vụ đi kèm.
 * Tách từ AdminConsole để tuân thủ Single Responsibility Principle.
 */
public class ServiceManagement {
    private static final IServiceItemService serviceItemService = new ServiceItemService();

    public static void manage() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- QUẢN LÝ DỊCH VỤ ĐI KÈM ---");
            System.out.println("1. Xem danh sách dịch vụ");
            System.out.println("2. Thêm dịch vụ mới");
            System.out.println("3. Sửa thông tin dịch vụ");
            System.out.println("4. Xóa dịch vụ");
            System.out.println("0. Quay lại menu chính");
            System.out.print("Lựa chọn: ");
            int choice = InputValidation.inputInt();

            switch (choice) {
                case 1 -> listServices();
                case 2 -> addService();
                case 3 -> updateService();
                case 4 -> deleteService();
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void listServices() {
        try {
            List<ServiceItem> services = serviceItemService.getAllServices();
            if (services.isEmpty()) {
                System.out.println("Hiện tại chưa có dịch vụ đi kèm nào trong hệ thống.");
            } else {
                System.out.println("\n--- DANH SÁCH DỊCH VỤ ĐI KÈM ---");
                System.out.printf("%-5s | %-25s | %-15s | %-15s | %-30s\n", "ID", "Tên Dịch Vụ", "Đơn Vị Tính", "Đơn Giá", "Mô tả");
                System.out.println("-------------------------------------------------------------------------------------------------");
                for (ServiceItem s : services) {
                    System.out.printf("%-5d | %-25s | %-15s | %-15.2f | %-30s\n",
                            s.getServiceId(), s.getServiceName(), s.getUnit(), s.getPrice(),
                            (s.getDescription() != null ? s.getDescription() : ""));
                }
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
        }
    }

    private static void addService() {
        System.out.print("Nhập tên dịch vụ: ");
        String name = InputValidation.inputString();
        System.out.print("Nhập đơn vị tính (vd: Chai, Người, Set): ");
        String unit = InputValidation.inputString();
        double price = inputPrice("Nhập đơn giá (VNĐ): ");
        System.out.print("Nhập mô tả dịch vụ: ");
        String description = InputValidation.inputString();

        try {
            if (serviceItemService.addService(name, unit, price, description)) {
                System.out.println("=> Thêm dịch vụ đi kèm thành công!");
            } else {
                System.out.println("=> Thêm dịch vụ thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi thêm dịch vụ: " + e.getMessage());
        }
    }

    private static void updateService() {
        System.out.print("Nhập ID dịch vụ cần sửa: ");
        int svId = InputValidation.inputInt();
        System.out.print("Nhập tên dịch vụ mới: ");
        String name = InputValidation.inputString();
        System.out.print("Nhập đơn vị tính mới: ");
        String unit = InputValidation.inputString();
        double price = inputPrice("Nhập đơn giá mới (VNĐ): ");
        System.out.print("Nhập mô tả mới: ");
        String description = InputValidation.inputString();

        try {
            if (serviceItemService.updateService(svId, name, unit, price, description)) {
                System.out.println("=> Cập nhật thông tin dịch vụ thành công!");
            } else {
                System.out.println("=> Cập nhật thông tin dịch vụ thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi cập nhật dịch vụ: " + e.getMessage());
        }
    }

    private static void deleteService() {
        System.out.print("Nhập ID dịch vụ cần xóa: ");
        int svId = InputValidation.inputInt();
        System.out.print("Bạn có chắc chắn muốn xóa dịch vụ ID " + svId + "? (Y/N): ");
        String confirm = InputValidation.inputString();
        if (confirm.equalsIgnoreCase("Y")) {
            try {
                if (serviceItemService.deleteService(svId)) {
                    System.out.println("=> Xóa dịch vụ thành công!");
                } else {
                    System.out.println("=> Xóa dịch vụ thất bại.");
                }
            } catch (Exception e) {
                System.out.println("=> Lỗi khi xóa dịch vụ (có thể do đang được sử dụng): " + e.getMessage());
            }
        } else {
            System.out.println("=> Đã hủy thao tác xóa.");
        }
    }

    /** Helper: nhập giá hợp lệ (loop cho đến khi nhập đúng số thực) */
    private static double inputPrice(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(InputValidation.inputString());
            } catch (NumberFormatException e) {
                System.out.println("Giá không hợp lệ, vui lòng nhập lại.");
            }
        }
    }
}
