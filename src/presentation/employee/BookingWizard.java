package presentation.employee;

import model.*;
import model.Enum.BookingStatus;
import model.Enum.PreparationStatus;
import service.IBookingService;
import service.IEquipmentService;
import service.IServiceItemService;
import service.BookingService;
import service.EquipmentService;
import service.ServiceItemService;
import util.InputValidation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tách từ EmployeeConsole để tuân thủ Single Responsibility Principle.
 * Chứa toàn bộ logic của Booking Wizard (6 bước) và xem/hủy lịch sử.
 */
public class BookingWizard {
    private static final IBookingService bookingService = new BookingService();
    private static final IEquipmentService equipmentService = new EquipmentService();
    private static final IServiceItemService serviceService = new ServiceItemService();

    /** Bước 1-8: Wizard đặt phòng họp */
    public static void start(User employee) {
        System.out.println("\n--- TIẾN TRÌNH ĐẶT PHÒNG HỌP (WIZARD) ---");
        try {
            // Bước 1: Nhập thời gian
            LocalDateTime[] times = inputDateTimeRange();
            if (times == null) return;
            LocalDateTime startDateTime = times[0];
            LocalDateTime endDateTime = times[1];

            // Bước 2: Số người tham gia (I-G: dùng inputPositiveInt tránh số âm/0)
            System.out.print("- Dự kiến bao nhiêu người tham gia? ");
            int capacity = InputValidation.inputPositiveInt();

            // Bước 3: Tìm & hiển thị phòng trống
            List<Room> availableRooms = bookingService.getAvailableRooms(startDateTime, endDateTime, capacity);
            if (availableRooms.isEmpty()) {
                System.out.println("=> Rất tiếc, hiện tại không có phòng nào trống và đủ sức chứa cho khung giờ này!");
                return;
            }
            printRoomTable(availableRooms);

            // Bước 4: Chọn phòng
            int roomId = selectRoom(availableRooms);
            if (roomId == -1) return;

            // Bước 5: Chọn thiết bị di động
            List<BookingDetailEquipment> eqList = selectEquipments();

            // Bước 6: Chọn dịch vụ đi kèm
            List<BookingDetailService> svList = selectServices();

            // Bước 7: Review tổng kết
            printReview(roomId, startDateTime, endDateTime, capacity, eqList, svList);

            // Bước 8: Xác nhận & Submit
            System.out.print("=> Xác nhận đẩy yêu cầu đặt phòng (Booking)? (Y/N): ");
            if (InputValidation.inputString().equalsIgnoreCase("Y")) {
                Booking booking = new Booking(0, employee.getUserId(), roomId, null,
                        startDateTime, endDateTime, capacity,
                        BookingStatus.PENDING, PreparationStatus.PENDING, null);
                if (bookingService.createBooking(booking, eqList, svList)) {
                    System.out.println(">> TẠO ĐẶT PHÒNG THÀNH CÔNG! Đơn của bạn đang ở trạng thái PENDING để Admin duyệt.");
                }
            } else {
                System.out.println("=> Hủy thao tác đặt phòng.");
            }

        } catch (DateTimeParseException e) {
            System.out.println("=> Lỗi: Định dạng ngày/thời gian không hợp lệ. Hãy đảm bảo nhập đúng chuẩn. Hủy thao tác.");
        } catch (Exception e) {
            System.out.println("=> Lỗi hệ thống khi đặt phòng: " + e.getMessage());
        }
    }

    /** Xem lịch sử đặt phòng & tùy chọn hủy */
    public static void viewHistory(User employee) {
        System.out.println("\n--- LỊCH SỬ ĐẶT PHÒNG HỌP ---");
        try {
            List<Booking> history = bookingService.getBookingHistory(employee.getUserId());
            if (history.isEmpty()) {
                System.out.println("=> Bạn chưa có lịch sử đặt phòng nào.");
                return;
            }

            System.out.printf("%-5s | %-10s | %-20s | %-20s | %-15s | %-15s\n",
                    "ID", "Room ID", "Start Time", "End Time", "Booking Status", "Prep Status");
            System.out.println("-------------------------------------------------------------------------------------------");
            for (Booking b : history) {
                System.out.printf("%-5d | %-10d | %-20s | %-20s | %-15s | %-15s\n",
                        b.getBookingId(), b.getRoomId(), b.getStartTime(), b.getEndTime(),
                        b.getBookingStatus(), b.getPreparationStatus());
            }

            System.out.println("\nTùy chọn:");
            System.out.println("1. Quay lại");
            System.out.println("2. Hủy một Booking (Chỉ áp dụng khi trạng thái PENDING)");
            System.out.print("Lựa chọn: ");
            int option = InputValidation.inputInt();

            if (option == 2) {
                cancelBooking(employee.getUserId());
            }

        } catch (Exception e) {
            System.out.println("=> Lỗi khi tải lịch sử đặt phòng: " + e.getMessage());
        }
    }

    // ---- Private helpers ----

    /** Nhập ngày + giờ bắt đầu + giờ kết thúc, trả về [startDateTime, endDateTime] hoặc null nếu lỗi */
    private static LocalDateTime[] inputDateTimeRange() throws DateTimeParseException {
        System.out.print("- Nhập Ngày họp (Định dạng yyyy-MM-dd): ");
        LocalDate date = LocalDate.parse(InputValidation.inputString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        System.out.print("- Nhập Giờ bắt đầu (Định dạng HH:mm): ");
        LocalTime startTime = LocalTime.parse(InputValidation.inputString(), DateTimeFormatter.ofPattern("HH:mm"));

        System.out.print("- Nhập Giờ kết thúc (Định dạng HH:mm): ");
        LocalTime endTime = LocalTime.parse(InputValidation.inputString(), DateTimeFormatter.ofPattern("HH:mm"));

        return new LocalDateTime[]{
                LocalDateTime.of(date, startTime),
                LocalDateTime.of(date, endTime)
        };
    }

    private static void printRoomTable(List<Room> rooms) {
        System.out.println("\n--- DANH SÁCH PHÒNG PHÙ HỢP ---");
        System.out.printf("%-5s | %-20s | %-10s | %-20s\n", "ID", "Tên Phòng", "Sức chứa", "Vị trí");
        System.out.println("-----------------------------------------------------");
        rooms.forEach(r -> System.out.printf("%-5d | %-20s | %-10d | %-20s\n",
                r.getRoomId(), r.getRoomName(), r.getCapacity(), r.getLocation()));
    }

    /** Chọn phòng từ danh sách, trả về roomId hợp lệ hoặc -1 nếu hủy */
    private static int selectRoom(List<Room> availableRooms) {
        System.out.print("- Nhập ID phòng muốn chọn: ");
        int roomId = InputValidation.inputInt();
        if (availableRooms.stream().noneMatch(r -> r.getRoomId() == roomId)) {
            System.out.println("ID phòng chọn không hợp lệ hoặc không có trong danh sách. Hủy thao tác.");
            return -1;
        }
        return roomId;
    }

    /** Chọn thiết bị di động cần mượn, trả về danh sách đã chọn */
    private static List<BookingDetailEquipment> selectEquipments() throws Exception {
        List<BookingDetailEquipment> eqList = new ArrayList<>();
        System.out.print("- Bạn có muốn mượn thêm thiết bị di động không? (Y/N): ");
        if (!InputValidation.inputString().equalsIgnoreCase("Y")) return eqList;

        // I-A: Lấy danh sách 1 LẦN trước vòng lặp — không gọi DB lặp đi lặp lại
        List<Equipment> equipments = equipmentService.getAllEquipments();
        if (equipments.isEmpty()) {
            System.out.println("=> Hiện không có thiết bị nào trong hệ thống.");
            return eqList;
        }

        while (true) {
            // Hiển thị từ cache cục bộ (0 DB calls)
            System.out.printf("\n%-5s | %-20s | %-10s | %-10s\n", "ID", "Tên thiết bị", "Tổng kho", "Sẵn có");
            equipments.forEach(eq -> System.out.printf("%-5d | %-20s | %-10d | %-10d\n",
                    eq.getEquipmentId(), eq.getEquipmentName(), eq.getQuantity(), eq.getAvailable()));

            System.out.print("Nhập ID thiết bị cần mượn (0 để dừng): ");
            int eqId = InputValidation.inputInt();
            if (eqId == 0) break;

            // I-F: Validate ID từ danh sách thực tế
            Equipment selected = equipments.stream()
                    .filter(e -> e.getEquipmentId() == eqId).findFirst().orElse(null);
            if (selected == null) {
                System.out.println("=> ID thiết bị không hợp lệ. Vui lòng chọn lại.");
                continue;
            }

            System.out.print("Nhập số lượng mượn: ");
            int qty = InputValidation.inputPositiveInt();  // I-G: chặn số âm/0

            // I-F: Kiểm tra số lượng khả dụng trong kho
            if (qty > selected.getAvailable()) {
                System.out.printf("=> Lỗi: Chỉ còn %d chiếc \"%s\" khả dụng!%n",
                        selected.getAvailable(), selected.getEquipmentName());
                continue;
            }

            eqList.add(new BookingDetailEquipment(0, eqId, qty));
            System.out.println("=> Bỏ thiết bị vào giỏ thành công.");
            System.out.print("Thêm thiết bị khác? (Y/N): ");
            if (!InputValidation.inputString().equalsIgnoreCase("Y")) break;
        }
        return eqList;
    }

    /** Chọn dịch vụ đi kèm, trả về danh sách đã chọn */
    private static List<BookingDetailService> selectServices() throws Exception {
        List<BookingDetailService> svList = new ArrayList<>();
        System.out.print("- Bạn có muốn yêu cầu thêm dịch vụ (Nước suối, Tea Break...)? (Y/N): ");
        if (!InputValidation.inputString().equalsIgnoreCase("Y")) return svList;

        // I-A: Lấy danh sách 1 LẦN trước vòng lặp — không gọi DB lặp đi lặp lại
        List<ServiceItem> services = serviceService.getAllServices();
        if (services.isEmpty()) {
            System.out.println("=> Hiện không có dịch vụ nào trong hệ thống.");
            return svList;
        }

        while (true) {
            // Hiển thị từ cache cục bộ (0 DB calls)
            System.out.printf("\n%-5s | %-25s | %-10s | %-10s\n", "ID", "Tên dịch vụ", "Đơn vị", "Đơn giá");
            services.forEach(s -> System.out.printf("%-5d | %-25s | %-10s | %-10.2f\n",
                    s.getServiceId(), s.getServiceName(), s.getUnit(), s.getPrice()));

            System.out.print("Nhập ID dịch vụ cần đặt (0 để dừng): ");
            int svId = InputValidation.inputInt();
            if (svId == 0) break;

            // Validate ID từ danh sách thực tế
            ServiceItem selected = services.stream()
                    .filter(s -> s.getServiceId() == svId).findFirst().orElse(null);
            if (selected == null) {
                System.out.println("=> ID dịch vụ không hợp lệ. Vui lòng chọn lại.");
                continue;
            }

            System.out.print("Nhập số lượng: ");
            int qty = InputValidation.inputPositiveInt();  // I-G: chặn số âm/0

            svList.add(new BookingDetailService(0, svId, qty));
            System.out.println("=> Đã lưu yêu cầu dịch vụ.");
            System.out.print("Thêm dịch vụ khác? (Y/N): ");
            if (!InputValidation.inputString().equalsIgnoreCase("Y")) break;
        }
        return svList;
    }

    private static void printReview(int roomId, LocalDateTime start, LocalDateTime end,
                                    int capacity, List<BookingDetailEquipment> eqList,
                                    List<BookingDetailService> svList) throws Exception {
        System.out.println("\n=== THÔNG TIN TỔNG KẾT (REVIEW BOOKING) ===");
        System.out.println("- Phòng chọn (ID): " + roomId);
        System.out.println("- Thời gian:       " + start + " -> " + end);
        System.out.println("- Số người:        " + capacity);
        System.out.println("- Thiết bị mượn:   " + eqList.size() + " loại");
        System.out.println("- Dịch vụ đặt:     " + svList.size() + " loại");

        double estCost = bookingService.calculateTotalServiceCost(svList);
        if (estCost > 0) {
            System.out.printf("- Chi phí dịch vụ ước tính: %,.0f VNĐ\n", estCost);
        }
    }

    private static void cancelBooking(int userId) throws Exception {
        System.out.print("Nhập ID Booking muốn hủy: ");
        int cancelId = InputValidation.inputInt();
        System.out.print("Bạn có chắc chắn muốn hủy Booking ID " + cancelId + "? (Y/N): ");
        if (InputValidation.inputString().equalsIgnoreCase("Y")) {
            if (bookingService.cancelBooking(cancelId, userId)) {
                System.out.println("=> ĐÃ HỦY BOOKING THÀNH CÔNG.");
            } else {
                System.out.println("=> Lỗi: Cập nhật thất bại. (Có thể do sai ID hoặc do Admin đã duyệt)");
            }
        }
    }
}
