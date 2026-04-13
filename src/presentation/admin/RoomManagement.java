package presentation.admin;

import model.Room;
import service.IRoomService;
import service.RoomService;
import util.InputValidation;
import java.util.List;

/**
 * Quản lý CRUD Phòng họp.
 * Tách từ AdminConsole để tuân thủ Single Responsibility Principle.
 */
public class RoomManagement {
    private static final IRoomService roomService = new RoomService();

    public static void manage() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- QUẢN LÝ PHÒNG HỌP ---");
            System.out.println("1. Xem danh sách phòng họp");
            System.out.println("2. Thêm phòng họp mới");
            System.out.println("3. Sửa thông tin phòng");
            System.out.println("4. Xóa phòng họp");
            System.out.println("5. Tìm kiếm phòng theo tên");
            System.out.println("0. Quay lại menu chính");
            System.out.print("Lựa chọn: ");
            int choice = InputValidation.inputInt();

            switch (choice) {
                case 1 -> listRooms();
                case 2 -> addRoom();
                case 3 -> updateRoom();
                case 4 -> deleteRoom();
                case 5 -> searchRoom();
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void listRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            if (rooms.isEmpty()) {
                System.out.println("Hiện tại chưa có phòng họp nào trong hệ thống.");
            } else {
                System.out.println("\n--- DANH SÁCH PHÒNG HỌP ---");
                System.out.printf("%-5s | %-22s | %-10s | %-22s | %-30s%n",
                        "ID", "Tên Phòng", "Sức chứa", "Vị trí", "Thiết bị cố định");
                System.out.println("-".repeat(99));
                for (Room r : rooms) {
                    System.out.printf("%-5d | %-22s | %-10d | %-22s | %-30s%n",
                            r.getRoomId(), r.getRoomName(), r.getCapacity(),
                            r.getLocation(), r.getFixedDevice());
                }
                System.out.println("Tổng: " + rooms.size() + " phòng.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi lấy danh sách phòng: " + e.getMessage());
        }
    }

    private static void addRoom() {
        System.out.print("Nhập tên phòng: ");
        String name = InputValidation.inputString();
        System.out.print("Nhập sức chứa (người): ");
        int capacity = InputValidation.inputInt();
        System.out.print("Nhập vị trí (vd: Tầng 3 - Tòa A): ");
        String location = InputValidation.inputString();
        System.out.print("Nhập thiết bị cố định (vd: Máy chiếu, Bảng trắng): ");
        String devices = InputValidation.inputString();

        try {
            if (roomService.addRoom(name, capacity, location, devices)) {
                System.out.println("=> Thêm phòng họp thành công!");
            } else {
                System.out.println("=> Thêm phòng thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Thêm phòng thất bại (Có thể do trùng tên): " + e.getMessage());
        }
    }

    private static void updateRoom() {
        System.out.print("Nhập ID phòng cần sửa: ");
        int roomId = InputValidation.inputInt();
        System.out.print("Nhập tên phòng mới: ");
        String name = InputValidation.inputString();
        System.out.print("Nhập sức chứa mới (người): ");
        int capacity = InputValidation.inputInt();
        System.out.print("Nhập vị trí mới: ");
        String location = InputValidation.inputString();
        System.out.print("Nhập thiết bị cố định mới: ");
        String devices = InputValidation.inputString();

        try {
            if (roomService.updateRoom(roomId, name, capacity, location, devices)) {
                System.out.println("=> Cập nhật thông tin phòng họp thành công!");
            } else {
                System.out.println("=> Cập nhật thông tin phòng họp thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi cập nhật phòng họp: " + e.getMessage());
        }
    }

    private static void deleteRoom() {
        System.out.print("Nhập ID phòng cần xóa: ");
        int roomId = InputValidation.inputInt();
        System.out.print("Bạn có chắc chắn muốn xóa phòng ID " + roomId + "? (Y/N): ");
        String confirm = InputValidation.inputString();
        if (confirm.equalsIgnoreCase("Y")) {
            try {
                if (roomService.deleteRoom(roomId)) {
                    System.out.println("=> Xóa phòng họp thành công!");
                } else {
                    System.out.println("=> Xóa phòng họp thất bại.");
                }
            } catch (Exception e) {
                System.out.println("=> Lỗi khi xóa phòng họp (có thể do phòng đang được đặt): " + e.getMessage());
            }
        } else {
            System.out.println("=> Đã hủy thao tác xóa.");
        }
    }

    private static void searchRoom() {
        System.out.print("Nhập tên phòng cần tìm: ");
        String keyword = InputValidation.inputString();
        try {
            List<Room> list = roomService.getRoomsByName(keyword);
            if (list.isEmpty()) {
                System.out.println("Không tìm thấy phòng nào có tên chứa: " + keyword);
            } else {
                System.out.println("\n--- KẾT QUẢ TÌM KIẾM ---");
                System.out.printf("%-5s | %-22s | %-10s | %-22s | %-30s%n",
                        "ID", "Tên Phòng", "Sức chứa", "Vị trí", "Thiết bị cố định");
                System.out.println("-".repeat(99));
                for (Room r : list) {
                    System.out.printf("%-5d | %-22s | %-10d | %-22s | %-30s%n",
                            r.getRoomId(), r.getRoomName(), r.getCapacity(),
                            r.getLocation(), r.getFixedDevice());
                }
                System.out.println("Tổng: " + list.size() + " phòng.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi tìm kiếm phòng: " + e.getMessage());
        }
    }
}
