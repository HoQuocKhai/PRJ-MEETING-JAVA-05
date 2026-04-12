package service;

import dao.ServiceItemDAO;
import model.ServiceItem;
import java.util.List;

public class ServiceItemService {
    private final ServiceItemDAO serviceItemDAO = new ServiceItemDAO();

    public void displayAllServices() {
        try {
            List<ServiceItem> services = serviceItemDAO.getAllServices();
            if (services.isEmpty()) {
                System.out.println("Hiện tại chưa có dịch vụ đi kèm nào trong hệ thống.");
                return;
            }
            System.out.println("\n--- DANH SÁCH DỊCH VỤ ĐI KÈM ---");
            System.out.printf("%-5s | %-25s | %-15s | %-15s | %-30s\n", "ID", "Tên Dịch Vụ", "Đơn Vị Tính", "Đơn Giá", "Mô tả");
            System.out.println("-------------------------------------------------------------------------------------------------");
            for (ServiceItem s : services) {
                System.out.printf("%-5d | %-25s | %-15s | %-15.2f | %-30s\n",
                        s.getServiceId(), s.getServiceName(), s.getUnit(), s.getPrice(), 
                        (s.getDescription() != null ? s.getDescription() : ""));
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
        }
    }

    public void addService(String name, String unit, double price, String description) {
        try {
            ServiceItem s = new ServiceItem(0, name, unit, price, description);
            if (serviceItemDAO.insertService(s)) {
                System.out.println("=> Thêm dịch vụ đi kèm thành công!");
            } else {
                System.out.println("=> Thêm dịch vụ thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi thêm dịch vụ: " + e.getMessage());
        }
    }

    public void updateService(int id, String name, String unit, double price, String description) {
        try {
            ServiceItem existing = serviceItemDAO.getServiceById(id);
            if (existing == null) {
                System.out.println("=> Không tìm thấy dịch vụ có ID: " + id);
                return;
            }
            existing.setServiceName(name);
            existing.setUnit(unit);
            existing.setPrice(price);
            existing.setDescription(description);
            
            if (serviceItemDAO.updateService(existing)) {
                System.out.println("=> Cập nhật thông tin dịch vụ thành công!");
            } else {
                System.out.println("=> Cập nhật thông tin dịch vụ thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi cập nhật dịch vụ: " + e.getMessage());
        }
    }

    public void deleteService(int id) {
        try {
            ServiceItem existing = serviceItemDAO.getServiceById(id);
            if (existing == null) {
                System.out.println("=> Không tìm thấy dịch vụ có ID: " + id);
                return;
            }
            if (serviceItemDAO.deleteService(id)) {
                System.out.println("=> Xóa dịch vụ thành công!");
            } else {
                System.out.println("=> Xóa dịch vụ thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi xóa dịch vụ (có thể do đang được sử dụng): " + e.getMessage());
        }
    }
}
