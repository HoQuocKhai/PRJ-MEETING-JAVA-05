package presentation;

import model.Booking;
import model.BookingDetailEquipment;
import model.BookingDetailService;
import model.Room;
import model.User;
import model.Enum.BookingStatus;
import model.Enum.PreparationStatus;
import service.BookingService;
import service.EquipmentService;
import service.ServiceItemService;
import dao.NotificationDAO;
import model.Notification;
import util.InputValidation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmployeeConsole {
    private static final BookingService bookingService = new BookingService();
    private static final EquipmentService equipmentService = new EquipmentService();
    private static final ServiceItemService serviceService = new ServiceItemService();
    private static final NotificationDAO notificationDAO = new NotificationDAO();

    public static void displayMenu(User employee) {
        // Thông báo chuông
        try {
            List<Notification> unread = notificationDAO.getUnreadNotifications(employee.getUserId());
            if (!unread.isEmpty()) {
                System.out.println("\n***************************************************");
                System.out.println("BẠN CÓ " + unread.size() + " THÔNG BÁO MỚI TỪ HỆ THỐNG!");
                for (Notification n : unread) {
                    System.out.println(" - " + n.getMessage() + " (" + n.getCreatedAt() + ")");
                }
                System.out.println("***************************************************");
                // Đánh dấu đã đọc
                notificationDAO.markAllAsRead(employee.getUserId());
            }
        } catch (Exception e) {
            System.err.println("=> Không thể lấy chuông báo: " + e.getMessage());
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n--- NHÂN VIÊN: " + employee.getUsername() + " ---");
            System.out.println("1. Đặt phòng họp");
            System.out.println("2. Yêu cầu dịch vụ văn phòng (Phát sinh)");
            System.out.println("3. Lịch sử đặt phòng");
            System.out.println("4. Xem/Cập nhật hồ sơ cá nhân");
            System.out.println("0. Đăng xuất");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> handleBookingWizard(employee);
                case 3 -> viewBookingHistory(employee);
                case 4 -> ProfileConsole.manageProfile(employee);
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void handleBookingWizard(User employee) {
        System.out.println("\n--- TIẾN TRÌNH ĐẶT PHÒNG HỌP (WIZARD) ---");
        try {
            // 1. Nhập Thời Gian
            System.out.print("- Nhập Ngày họp (Định dạng yyyy-MM-dd): ");
            String dateStr = InputValidation.inputString();
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            System.out.print("- Nhập Giờ bắt đầu (Định dạng HH:mm): ");
            String startStr = InputValidation.inputString();
            LocalTime startTime = LocalTime.parse(startStr, DateTimeFormatter.ofPattern("HH:mm"));

            System.out.print("- Nhập Giờ kết thúc (Định dạng HH:mm): ");
            String endStr = InputValidation.inputString();
            LocalTime endTime = LocalTime.parse(endStr, DateTimeFormatter.ofPattern("HH:mm"));

            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

            // 2. Nhập số lượng tham gia
            System.out.print("- Dự kiến bao nhiêu người tham gia? ");
            int capacity = InputValidation.inputInt();

            // 3. Tìm & Hiển thị Phòng
            List<Room> availableRooms = bookingService.getAvailableRooms(startDateTime, endDateTime, capacity);
            if (availableRooms.isEmpty()) {
                System.out.println("=> Rất tiếc, hiện tại không có phòng nào trống và đủ sức chứa cho khung giờ này!");
                return; // Thoát wizard
            }

            System.out.println("\n--- DANH SÁCH PHÒNG PHÙ HỢP ---");
            System.out.printf("%-5s | %-20s | %-10s | %-20s\n", "ID", "Tên Phòng", "Sức chứa", "Vị trí");
            for (Room r : availableRooms) {
                System.out.printf("%-5d | %-20s | %-10d | %-20s\n", r.getRoomId(), r.getRoomName(), r.getCapacity(), r.getLocation());
            }

            // Chọn Phòng
            System.out.print("- Nhập ID phòng muốn chọn: ");
            int roomId = InputValidation.inputInt();
            boolean validRoom = availableRooms.stream().anyMatch(r -> r.getRoomId() == roomId);
            if (!validRoom) {
                System.out.println("ID phòng chọn không hợp lệ hoặc không có trong danh sách khả dụng. Hủy thao tác.");
                return;
            }

            // 4. Mượn trang thiết bị di động
            List<BookingDetailEquipment> eqList = new ArrayList<>();
            System.out.print("- Bạn có muốn mượn thêm thiết bị di động không? (Y/N): ");
            if (InputValidation.inputString().equalsIgnoreCase("Y")) {
                boolean addMore = true;
                while (addMore) {
                    equipmentService.displayAllEquipments();
                    System.out.print("Nhập ID thiết bị cần mượn (Nhập 0 để dừng): ");
                    int eqId = InputValidation.inputInt();
                    if (eqId == 0) break;
                    
                    System.out.print("Nhập số lượng mượn: ");
                    int eqQty = InputValidation.inputInt();
                    if (eqQty > 0) {
                        eqList.add(new BookingDetailEquipment(0, eqId, eqQty));
                        System.out.println("=> Bỏ thiết bị vào giỏ thành công.");
                    }
                    
                    System.out.print("Thêm thiết bị khác? (Y/N): ");
                    addMore = InputValidation.inputString().equalsIgnoreCase("Y");
                }
            }

            // 5. Thêm Dịch vụ nước uống / tea break
            List<BookingDetailService> svList = new ArrayList<>();
            System.out.print("- Bạn có muốn yêu cầu thêm dịch vụ (Nước suối, Tea Break...) không? (Y/N): ");
            if (InputValidation.inputString().equalsIgnoreCase("Y")) {
                boolean addMore = true;
                while (addMore) {
                    serviceService.displayAllServices();
                    System.out.print("Nhập ID dịch vụ cần đặt (Nhập 0 để dừng): ");
                    int svId = InputValidation.inputInt();
                    if (svId == 0) break;
                    
                    System.out.print("Nhập số lượng: ");
                    int svQty = InputValidation.inputInt();
                    if (svQty > 0) {
                        svList.add(new BookingDetailService(0, svId, svQty));
                        System.out.println("=> Đã lưu yêu cầu dịch vụ.");
                    }
                    
                    System.out.print("Thêm dịch vụ khác? (Y/N): ");
                    addMore = InputValidation.inputString().equalsIgnoreCase("Y");
                }
            }

            // 6. Tính tổng và Review
            System.out.println("\n=== THÔNG TIN TỔNG KẾT (REVIEW BOOKING) ===");
            System.out.println("- Phòng chọn (ID): " + roomId);
            System.out.println("- Thời gian: " + startDateTime + " -> " + endDateTime);
            System.out.println("- Số người: " + capacity);
            System.out.println("- Số thiết bị mượn thêm: " + eqList.size() + " loại.");
            System.out.println("- Số dịch vụ phát sinh: " + svList.size() + " loại.");
            
            double estCost = bookingService.calculateTotalServiceCost(svList);
            if (estCost > 0) {
                System.out.println("-> Cước phí dịch vụ ước tính: " + estCost + " VND");
            }

            // Xác nhận
            System.out.print("=> Xác nhận đẩy yêu cầu đặt phòng (Booking) ? (Y/N): ");
            if (InputValidation.inputString().equalsIgnoreCase("Y")) {
                // Tạo Model Booking để đẩy xuống Database
                Booking booking = new Booking(0, employee.getUserId(), roomId, null,
                                              startDateTime, endDateTime, capacity, 
                                              BookingStatus.PENDING, PreparationStatus.PENDING, null);
                
                if (bookingService.createBooking(booking, eqList, svList)) {
                    System.out.println(">> TẠO ĐẶT PHÒNG THÀNH CÔNG! Đơn của bạn đang ở trạng thái PENDING để Admin duyệt.");
                }
            } else {
                System.out.println("=> Hủy thao tác đặt phòng.");
            }

        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("=> Lỗi: Định dạng ngày/thời gian không hợp lệ. Hãy đảm bảo nhập đúng chuẩn. Hủy thao tác.");
        } catch (Exception e) {
            System.out.println("=> Lỗi hệ thống khi đặt phòng: " + e.getMessage());
        }
    }

    private static void viewBookingHistory(User employee) {
        System.out.println("\n--- LỊCH SỬ ĐẶT PHÒNG HỌP ---");
        try {
            List<Booking> history = bookingService.getBookingHistory(employee.getUserId());
            if (history.isEmpty()) {
                System.out.println("=> Bạn chưa có lịch sử đặt phòng nào.");
                return;
            }

            System.out.printf("%-5s | %-10s | %-20s | %-20s | %-15s | %-15s\n", 
                    "ID", "Room ID", "Start Time", "End Time", "Booking Status", "Prep Status");
            for (Booking b : history) {
                System.out.printf("%-5d | %-10d | %-20s | %-20s | %-15s | %-15s\n", 
                        b.getBookingId(), b.getRoomId(), b.getStartTime(), b.getEndTime(), 
                        b.getBookingStatus(), b.getPreparationStatus());
            }

            System.out.println("\nTùy chọn:");
            System.out.println("1. Quay lại Menu chính");
            System.out.println("2. Hủy một Booking (Chỉ áp dụng cho trạng thái PENDING)");
            System.out.print("Lựa chọn: ");
            int option = InputValidation.inputInt();

            if (option == 2) {
                System.out.print("Nhập ID Booking muốn hủy: ");
                int cancelId = InputValidation.inputInt();
                
                System.out.print("Bạn có chắc chắn muốn hủy Booking ID " + cancelId + "? (Y/N): ");
                if (InputValidation.inputString().equalsIgnoreCase("Y")) {
                    if (bookingService.cancelBooking(cancelId, employee.getUserId())) {
                        System.out.println("=> ĐÃ HỦY BOOKING THÀNH CÔNG.");
                    } else {
                        System.out.println("=> Lỗi: Cập nhật thất bại. (Có thể do sai ID hoặc do Admin đã duyệt)");
                    }
                }
            } else {
                System.out.println("=> Đã hủy thao tác.");
            }

        } catch (Exception e) {
            System.out.println("=> Lỗi khi tải lịch sử đặt phòng: " + e.getMessage());
        }
    }
}
