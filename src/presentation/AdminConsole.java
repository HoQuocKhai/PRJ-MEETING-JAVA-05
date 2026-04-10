package presentation;

import model.Enum.Role;
import model.User;
import service.RoomService;
import service.UserService;
import util.InputValidation;

public class AdminConsole {
    private static final UserService userService = new UserService();
    private static final RoomService roomService = new RoomService();

    public static void displayMenu(User admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- QUẢN TRỊ VIÊN: " + admin.getUsername() + " ---");
            System.out.println("1. Quản lý người dùng (Thêm Admin/Support Staff)");
            System.out.println("2. Quản lý phòng họp");
            System.out.println("3. Quản lý thiết bị di động.");
            System.out.println("4. Xem báo cáo hệ thống");
            System.out.println("5. Xem/Cập nhật hồ sơ cá nhân");
            System.out.println("0. Đăng xuất");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> createStaff();
                case 2 -> manageRooms();
                case 3 -> System.out.println("Quản lý thiết bị di động.");
                case 5 -> ProfileConsole.manageProfile(admin);
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    // Thêm tk staff / admin
    public static void createStaff() {
        {
            System.out.println("\n--- THÊM NHÂN SỰ MỚI (ADMIN / SUPPORT STAFF) ---");
            System.out.println("Chọn vai trò muốn tạo:");
            System.out.println("1. Quản trị viên (ADMIN)");
            System.out.println("2. Nhân viên hỗ trợ (SUPPORT_STAFF)");
            System.out.print("Lựa chọn: ");
            int choice = InputValidation.inputInt();

            Role selectedRole = null;
            if (choice == 1) {
                selectedRole = Role.ADMIN;
            } else if (choice == 2) {
                selectedRole = Role.SUPPORT_STAFF;
            } else if (choice == 0) {
                System.out.println("Thoát thao tác thêm mới!");
                return;
            } else {
                System.out.println("Lựa chọn không hợp lệ. Hủy thao tác thêm mới!");
                return;
            }

            // Nếu chọn đúng, bắt đầu cho nhập thông tin
            System.out.print("Nhập Username: ");
            String username = InputValidation.inputString();

            try {
                if (userService.checkUsername(username)) {
                    System.out.println("Lỗi: Username '" + username + "' đã tồn tại! Vui lòng thao tác lại và chọn tên khác.");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Lỗi kiểm tra hệ thống: " + e.getMessage());
                return;
            }// check chùng username

            System.out.print("Nhập Password: ");
            String password = InputValidation.inputString();

            System.out.print("Nhập Phòng ban (Department): ");
            String department = InputValidation.inputString();

            String contact = InputValidation.inputEmail();
            String phone = InputValidation.inputPhoneNumber();

            try {
                userService.createStaffAdmin(username, password, selectedRole, department, contact, phone);
                System.out.println("=> Thêm " + selectedRole.name() + " thành công!");
            } catch (Exception e) {
                System.out.println("=> Thêm thất bại: " + e.getMessage());
            }
        }
    }

    // Quản lý phòng họp
    public static void manageRooms() {
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
                case 1 -> roomService.displayAllRooms();
                case 2 -> {
                    System.out.print("Nhập tên phòng: ");
                    String name = InputValidation.inputString();
                    System.out.print("Nhập sức chứa (người): ");
                    int capacity = InputValidation.inputInt();
                    System.out.print("Nhập vị trí (vd: Tầng 3 - Tòa A): ");
                    String location = InputValidation.inputString();
                    System.out.print("Nhập thiết bị cố định (vd: Máy chiếu, Bảng trắng): ");
                    String devices = InputValidation.inputString();

                    roomService.addRoom(name, capacity, location, devices);
                }// add
                case 3 -> {
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

                    roomService.updateRoom(roomId, name, capacity, location, devices);
                }// update
                case 4 -> {
                    System.out.print("Nhập ID phòng cần xóa: ");
                    int roomId = InputValidation.inputInt();
                    System.out.print("Bạn có chắc chắn muốn xóa phòng ID " + roomId + "? (Y/N): ");
                    String confirm = InputValidation.inputString();
                    if (confirm.equalsIgnoreCase("Y")) {
                        roomService.deleteRoom(roomId);
                    } else {
                        System.out.println("=> Đã hủy thao tác xóa.");
                    }
                }// delete
                case 5 -> {
                    System.out.print("Nhập tên phòng cần tìm: ");
                    String keyword = InputValidation.inputString();
                    roomService.displayRoomsByName(keyword);
                }// search by name
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    // Quản lý thiết bị di động
    public static void manageEquipments() {
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
                case 1 -> roomService.displayAllRooms();
                case 2 -> {

                }// add
                case 3 -> {
                }// update
                case 4 -> {

                }// delete
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }
}
