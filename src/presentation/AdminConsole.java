package presentation;

import model.Booking;
import model.Enum.Role;
import model.User;
import service.RoomService;
import service.UserService;
import util.InputValidation;

import java.util.List;

public class AdminConsole {
    private static final UserService userService = new UserService();
    private static final RoomService roomService = new RoomService();
    private static final service.EquipmentService equipmentService = new service.EquipmentService();
    private static final service.BookingService bookingService = new service.BookingService();

    public static void displayMenu(User admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- QUẢN TRỊ VIÊN: " + admin.getUsername() + " ---");
            System.out.println("1. Quản lý người dùng (Thêm Admin/Support Staff)");
            System.out.println("2. Quản lý phòng họp");
            System.out.println("3. Quản lý thiết bị di động.");
            System.out.println("4. Quản lý Đặt phòng (Duyệt/Từ chối)");
            System.out.println("5. Báo cáo Thống kê & Xuất Hóa đơn");
            System.out.println("6. Xem/Cập nhật hồ sơ cá nhân");
            System.out.println("0. Đăng xuất");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> createStaff();
                case 2 -> manageRooms();
                case 3 -> manageEquipments();
                case 4 -> manageBookings();
                case 5 -> viewReportsAndExport();
                case 6 -> ProfileConsole.manageProfile(admin);
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
                case 1 -> equipmentService.displayAllEquipments();
                case 2 -> {
                    System.out.print("Nhập tên thiết bị: ");
                    String name = InputValidation.inputString();
                    System.out.print("Nhập số lượng: ");
                    int quantity = InputValidation.inputInt();
                    System.out.print("Nhập số lượng sẵn có: ");
                    int available = InputValidation.inputInt();
                    System.out.print("Nhập trạng thái (ACTIVE, MAINTENANCE, BROKEN): ");
                    String statusStr = InputValidation.inputString();
                    model.Enum.EquipmentStatus status;
                    try {
                        status = model.Enum.EquipmentStatus.valueOf(statusStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Trạng thái không hợp lệ. Hủy thao tác.");
                        break;
                    }
                    equipmentService.addEquipment(name, quantity, available, status);
                }// add
                case 3 -> {
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
                    model.Enum.EquipmentStatus status;
                    try {
                        status = model.Enum.EquipmentStatus.valueOf(statusStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Trạng thái không hợp lệ. Hủy thao tác.");
                        break;
                    }
                    equipmentService.updateEquipment(eqId, name, quantity, available, status);
                }// update
                case 4 -> {
                    System.out.print("Nhập ID thiết bị cần xóa: ");
                    int eqId = InputValidation.inputInt();
                    System.out.print("Bạn có chắc chắn muốn xóa thiết bị ID " + eqId + "? (Y/N): ");
                    String confirm = InputValidation.inputString();
                    if (confirm.equalsIgnoreCase("Y")) {
                        equipmentService.deleteEquipment(eqId);
                    } else {
                        System.out.println("=> Đã hủy thao tác xóa.");
                    }
                }// delete
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    // Quản lý duyệt/từ chối Booking
    public static void manageBookings() {
        System.out.println("\n--- QUẢN LÝ ĐẶT PHÒNG (DUYỆT/TỪ CHỐI) ---");
        try {
            List<Booking> pendingList = bookingService.getPendingBookings();
            if (pendingList.isEmpty()) {
                System.out.println("=> Không có yêu cầu đặt phòng nào đang chờ duyệt (PENDING).");
                return;
            }

            System.out.println("Danh sách các Booking PENDING:");
            System.out.printf("%-5s | %-10s | %-10s | %-20s | %-20s\n", "ID", "User ID", "Room ID", "Start Time", "End Time");
            for (model.Booking b : pendingList) {
                System.out.printf("%-5d | %-10d | %-10d | %-20s | %-20s\n", 
                        b.getBookingId(), b.getUserId(), b.getRoomId(), b.getStartTime(), b.getEndTime());
            }

            System.out.print("Nhập ID Booking muốn xử lý (hoặc 0 để thoát): ");
            int bookingId = InputValidation.inputInt();
            if (bookingId == 0) return;

            System.out.println("Bạn muốn (1) Duyệt hay (2) Từ chối?");
            System.out.print("Lựa chọn: ");
            int action = InputValidation.inputInt();

            if (action == 2) {
                if (bookingService.rejectBooking(bookingId)) {
                    System.out.println("=> Đã TỪ CHỐI yêu cầu đặt phòng thành công.");
                } else {
                    System.out.println("=> Từ chối thất bại.");
                }
            } else if (action == 1) {
                List<User> staffList = userService.getSupportStaffs();
                if (staffList.isEmpty()) {
                    System.out.println("=> Không có nhân viên Support Staff nào trong hệ thống! Không thể duyệt gán việc.");
                    return;
                }

                System.out.println("\nDanh sách nhân viên Support Staff:");
                for (User staff : staffList) {
                    System.out.println("ID: " + staff.getUserId() + " - Name: " + staff.getUsername());
                }

                System.out.print("Nhập ID Nhân viên hỗ trợ để phân công: ");
                int staffId = InputValidation.inputInt();

                boolean validStaff = staffList.stream().anyMatch(s -> s.getUserId() == staffId);
                if (!validStaff) {
                    System.out.println("=> Lỗi: ID nhân viên không hợp lệ. Hủy thao tác.");
                    return;
                }

                if (bookingService.approveBooking(bookingId, staffId)) {
                    System.out.println("=> DUYỆT ĐẶT PHÒNG THÀNH CÔNG! Đã phân công cho Support Staff ID " + staffId);
                }
            } else {
                System.out.println("=> Lựa chọn không hợp lệ. Hủy thao tác.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi xử lý duyệt đặt phòng: " + e.getMessage());
        }
    }

    private static void viewReportsAndExport() {
        System.out.println("\n--- TỔNG QUAN BÁO CÁO & THỐNG KÊ ---");
        try {
            // 1. In thống kê phòng
            bookingService.printRoomUsageStatistics();

            // 2. In doanh thu tháng hiện tại
            java.time.LocalDate now = java.time.LocalDate.now();
            double monthlyRevenue = bookingService.calculateCompletedRevenue(now.getMonthValue(), now.getYear());
            System.out.println("\n- Tổng doanh thu Dịch vụ (Tháng " + now.getMonthValue() + "/" + now.getYear() + "): " 
                    + String.format("%,.0f VNĐ", monthlyRevenue));
            
            // 3. Sub-menu Xuất bill
            System.out.println("\nBạn có muốn XUẤT HÓA ĐƠN cho một cuộc họp đã hoàn tất (READY) không?");
            System.out.println("1. Xuất hóa đơn ra File (.txt)");
            System.out.println("0. Quay Lại");
            System.out.print("Lựa chọn: ");
            int option = InputValidation.inputInt();

            if (option == 1) {
                System.out.print("Nhập Booking ID cần xuất hóa đơn: ");
                int bId = InputValidation.inputInt();
                bookingService.exportBill(bId);
            }

        } catch (Exception e) {
            System.out.println("=> Lỗi hệ thống Báo cáo: " + e.getMessage());
        }
    }
}
