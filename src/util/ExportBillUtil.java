package util;

import model.Booking;
import model.dto.BookingEquipmentDetail;
import model.dto.BookingServiceDetail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportBillUtil {

    private static final String BILL_DIR = "out/bills/";

    public static boolean exportBillToFile(Booking b, double totalServiceCost, List<BookingEquipmentDetail> eqList, List<BookingServiceDetail> svList) {
        // Đảm bảo thư mục tồn tại
        File directory = new File(BILL_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                System.err.println("=> Lỗi: Không thể tạo thư mục " + BILL_DIR);
                return false;
            }
        }

        // Tạo tên file
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = BILL_DIR + "Bill_Booking_" + b.getBookingId() + "_" + timestamp + ".txt";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("==================================================\n");
            writer.write("           HÓA ĐƠN DỊCH VỤ PHÒNG HỌP              \n");
            writer.write("==================================================\n");
            writer.write("Mã Hóa Đơn (Booking ID) : " + b.getBookingId() + "\n");
            writer.write("Mã Người dùng (User ID) : " + b.getUserId() + "\n");
            writer.write("Mã Phòng (Room ID)      : " + b.getRoomId() + "\n");
            writer.write("Thời gian bắt đầu       : " + b.getStartTime() + "\n");
            writer.write("Thời gian kết thúc      : " + b.getEndTime() + "\n");
            writer.write("--------------------------------------------------\n");
            
            writer.write("\n[THIẾT BỊ HỖ TRỢ ĐÃ MƯỢN]\n");
            if (eqList == null || eqList.isEmpty()) {
                writer.write("=> Không sử dụng thiết bị kèm theo.\n");
            } else {
                for (BookingEquipmentDetail eq : eqList) {
                    writer.write(String.format("- %-25s : %d (Thiết bị nội bộ - Miễn phí)\n",
                            eq.getEquipmentName(), eq.getBorrowedQuantity()));
                }
            }

            writer.write("\n[DỊCH VỤ PHÁT SINH]\n");
            if (svList == null || svList.isEmpty()) {
                writer.write("=> Không sử dụng dịch vụ trả phí.\n");
            } else {
                for (BookingServiceDetail sv : svList) {
                    double lineTotal = sv.getPrice() * sv.getOrderedQuantity();
                    writer.write(String.format("- %-20s : %d %-10s x %,.0f VND = %,.0f VND\n",
                            sv.getServiceName(), sv.getOrderedQuantity(), sv.getUnit(), sv.getPrice(), lineTotal));
                }
            }

            writer.write("--------------------------------------------------\n");
            writer.write(String.format("TỔNG CHI PHÍ DỊCH VỤ    : %,.0f VNĐ\n", totalServiceCost));
            writer.write("TRẠNG THÁI              : ĐÃ THANH TOÁN (READY)\n");
            writer.write("==================================================\n");
            writer.write("\nXin trân trọng cảm ơn!\n");
            
            System.out.println("=> Đã xuất hóa đơn thành công tại: " + fileName);
            return true;

        } catch (IOException e) {
            System.err.println("=> Lỗi rủi ro I/O khi ghi file hóa đơn: " + e.getMessage());
            return false;
        }
    }
}
