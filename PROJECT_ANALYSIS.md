# Phân tích Dự án: Hệ thống Quản lý Đặt phòng họp & Dịch vụ Văn phòng

**Mã dự án:** PRJ-MEETING-JAVA-05
**Môn học:** [IT203B-K24] - Java Advanced
**Ngày phân tích cập nhật:** 2026-04-13

---

## 1. Tổng quan yêu cầu dự án (Dựa trên SRS)

### Mục tiêu
Xây dựng một ứng dụng Java Console giúp quản lý việc đặt phòng họp, thiết bị hội nghị và các dịch vụ đi kèm trong môi trường văn phòng/công ty.

### Vai trò người dùng (Roles)
| Role | Mô tả |
|---|---|
| **EMPLOYEE** | Đăng ký, đăng nhập, đặt phòng, mượn thiết bị/dịch vụ, xem và hủy lịch họp (khi PENDING). |
| **SUPPORT_STAFF** | Xem danh sách nhiệm vụ được phân công, cập nhật trạng thái chuẩn bị phòng. |
| **ADMIN** | CRUD phòng/thiết bị/dịch vụ, duyệt/từ chối đặt phòng, phân công Support Staff, quản lý người dùng. |

### Yêu cầu kỹ thuật bắt buộc
- **Ngôn ngữ:** Java Core (OOP), tương tác console.
- **CSDL:** MySQL, kết nối qua JDBC thuần.
- **Kiến trúc:** Phân tầng rõ ràng: `Model` → `DAO` → `Service` → `Presentation` → `Util/Exception`.
- **Bảo mật:** Hash mật khẩu, kiểm soát phân quyền, validation input.

---

## 2. Hiện trạng codebase (Current State)

### 2.1 Cấu trúc thư mục

```
src/
├── dao/
│   ├── BaseDAO.java           ✅ Generic abstract class (Template Method Pattern)
│   ├── BookingDAO.java        ✅ Hoàn chỉnh, có Transaction
│   ├── EquipmentDAO.java      ⚠️ Có lỗi SQL (xem phần 3.1)
│   ├── NotificationDAO.java   ✅ Hoàn chỉnh
│   ├── RoomDAO.java           ✅ CRUD hoàn chỉnh
│   ├── ServiceItemDAO.java    ✅ CRUD hoàn chỉnh
│   └── UserDAO.java           ⚠️ Không extends BaseDAO (nhất quán với DAO khác)
├── exception/
│   └── InvalidRegisterException.java  ✅ Có
├── model/
│   ├── Enum/
│   │   ├── BookingStatus.java      ✅ (PENDING, APPROVED, REJECTED, CANCELED)
│   │   ├── EquipmentStatus.java    ✅ (ACTIVE, MAINTENANCE, BROKEN)
│   │   ├── PreparationStatus.java  ✅ (PENDING, PREPARING, READY, MISSING_EQUIPMENT)
│   │   └── Role.java               ✅ (EMPLOYEE, SUPPORT_STAFF, ADMIN)
│   ├── Booking.java                ✅ Đầy đủ fields
│   ├── BookingDetailEquipment.java ✅ DTO mapping n-n
│   ├── BookingDetailService.java   ✅ DTO mapping n-n
│   ├── Equipment.java              ✅
│   ├── Notification.java           ✅ (Tính năng nâng cao)
│   ├── Room.java                   ✅
│   ├── ServiceItem.java            ✅ (Có thêm orderQuantity DTO)
│   └── User.java                   ✅
├── presentation/
│   ├── AppConsole.java             ✅ Main Router
│   ├── AuthConsole.java            ✅ Login / Register
│   ├── AdminConsole.java           ✅ Menu Admin đầy đủ
│   ├── EmployeeConsole.java        ✅ Booking Wizard hoàn chỉnh
│   ├── ProfileConsole.java         ✅ Xem/Sửa profile
│   └── SupportStaffConsole.java    ✅ Cập nhật nhiệm vụ
├── service/
│   ├── BookingService.java         ⚠️ Có 2 phương thức vi phạm nguyên tắc (xem 3.2)
│   ├── EquipmentService.java       ⚠️ Vi phạm nguyên tắc phân tầng (xem 3.2)
│   ├── RoomService.java            ⚠️ Vi phạm nguyên tắc phân tầng (xem 3.2)
│   ├── ServiceItemService.java     ⚠️ Vi phạm nguyên tắc phân tầng (xem 3.2)
│   ├── SupportStaffService.java    ⚠️ Có 1 phương thức vi phạm (xem 3.2)
│   └── UserService.java            ✅ Đúng nguyên tắc – trả về data, không print
└── util/
    ├── DatabaseConnection.java     ✅
    ├── ExportBillUtil.java         ✅ Xuất hóa đơn ra file .txt
    ├── InputValidation.java        ✅ Validate email, phone, int, string
    └── PasswordUtil.java           ✅ BCrypt hash
```

### 2.2 Các luồng hoạt động (Activity Flows)

---

#### Luồng 1: Khởi động & Điều hướng (AppConsole)
```
Chạy AppConsole.main()
  → Nếu chưa đăng nhập: hiển thị menu [Đăng nhập / Đăng ký / Thoát]
    → [1] Đăng nhập → AuthConsole.login() → trả về User
    → [2] Đăng ký  → AuthConsole.register() → tạo tài khoản Employee mới
  → Nếu đã đăng nhập: phân nhánh theo Role:
    → ADMIN         → AdminConsole.displayMenu()
    → EMPLOYEE      → EmployeeConsole.displayMenu()
    → SUPPORT_STAFF → SupportStaffConsole.displayMenu()
  → Sau khi thoát menu con: loggedInUser = null → quay lại màn hình chính
```
**Đánh giá:** ✅ Luồng chính xác. Quản lý session bằng static field `loggedInUser`.

---

#### Luồng 2: Đăng ký & Đăng nhập (AuthConsole)
```
register():
  inputUsername() → loop check trùng (gọi UserService.checkUsername)
  inputConfirmPassword() → loop xác nhận khớp
  inputEmail() → validate regex
  inputPhoneNumber() → validate regex
  → UserService.registerEmployee() → PasswordUtil.hashPassword() → UserDAO.insertUser()

login():
  Nhập username / password
  → UserService.login() → UserDAO.getUserByUsername()
  → PasswordUtil.checkPassword(plainText, hashedFromDB)
  → Trả về User object hoặc null
```
**Đánh giá:** ✅ Mật khẩu được hash trước khi lưu (BCrypt). Validate đầy đủ.

---

#### Luồng 3: Đặt phòng - Employee Booking Wizard (EmployeeConsole)
```
handleBookingWizard(employee):
  1. Nhập ngày (yyyy-MM-dd) + giờ bắt đầu (HH:mm) + giờ kết thúc (HH:mm)
  2. Nhập số người tham gia
  3. BookingService.getAvailableRooms() → validate (startTime không ở quá khứ, end > start)
     → BookingDAO.getAvailableRooms() [SQL chống overlap: startTime < req.end AND endTime > req.start]
  4. Hiển thị danh sách phòng trống → Người dùng chọn roomId
  5. (Tùy chọn) Bọ vào giỏ thiết bị di động (BookingDetailEquipment)
  6. (Tùy chọn) Bọ vào giỏ dịch vụ đi kèm (BookingDetailService)
  7. Review thông tin tổng kết + ước tính chi phí dịch vụ
  8. Xác nhận → BookingService.createBooking()
     → BookingDAO.insertBooking() [SQL TRANSACTION: insert bookings + booking_equipments + booking_services]
     → Trạng thái ban đầu: bookingStatus=PENDING, preparationStatus=PENDING
```
**Đánh giá:** ✅ Logic wizard rõ ràng. Transaction đảm bảo tính toàn vẹn dữ liệu.

---

#### Luồng 4: Xem lịch & Hủy đặt phòng (EmployeeConsole)
```
viewBookingHistory(employee):
  BookingService.getBookingHistory(userId) → hiển thị bảng lịch sử
  Option 2 → Nhập bookingId → Xác nhận → BookingService.cancelBooking(id, userId)
    → validate: booking tồn tại, userId khớp, status phải là PENDING
    → BookingDAO.cancelBooking() [UPDATE WHERE bookingStatus='PENDING']
```
**Đánh giá:** ✅ Đúng yêu cầu SRS - chỉ hủy khi PENDING. Có kiểm tra ownership.

---

#### Luồng 5: Admin duyệt/từ chối đặt phòng (AdminConsole)
```
manageBookings():
  BookingService.getPendingBookings() → hiển thị danh sách
  Chọn bookingId
  [1] Duyệt:
    → UserService.getSupportStaffs() → hiển thị danh sách Staff
    → Chọn staffId (validate trong danh sách)
    → BookingService.approveBooking(bookingId, staffId)
       → Kiểm tra booking tồn tại & đang PENDING
       → Double-check phòng vẫn còn trống (chống race condition)
       → BookingDAO.approveAndAssign() [UPDATE SET status=APPROVED, supportStaffId=?, preparationStatus=PREPARING]
       → NotificationDAO.insertNotification() → gửi thông báo cho Employee
  [2] Từ chối:
    → BookingService.rejectBooking() [UPDATE SET status=REJECTED]
    → NotificationDAO.insertNotification() → gửi thông báo từ chối
```
**Đánh giá:** ✅ Luồng chuẩn. Có double-check xung đột, có thông báo tự động.

---

#### Luồng 6: Support Staff cập nhật trạng thái chuẩn bị (SupportStaffConsole)
```
handleSupportTasks(support):
  SupportStaffService.getTasksByStaffId() → lấy booking được phân công (status=APPROVED)
  Hiển thị bảng, chọn bookingId
  → SupportStaffService.printTaskDetails() → in chi tiết phòng, thiết bị, dịch vụ cần chuẩn bị
  Chọn trạng thái mới:
    [1] PREPARING → [2] READY → [3] MISSING_EQUIPMENT
  → SupportStaffService.updateTaskStatus() → BookingDAO.updatePreparationStatus()
```
**Đánh giá:** ✅ Đúng SRS. Tuy nhiên `SupportStaffService.printTaskDetails()` vi phạm nguyên tắc phân tầng (xem mục 3.2).

---

#### Luồng 7: Admin quản lý tài nguyên (Phòng / Thiết bị / Dịch vụ)
```
manageRooms()     → CRUD Room    → RoomService    → RoomDAO     ✅ Đầy đủ
manageEquipments() → CRUD Equip  → EquipmentService → EquipmentDAO ⚠️ Bug SQL (xem 3.1)
(Thiếu) ManageServices → CRUD ServiceItem → ServiceItemService → ServiceItemDAO
```
**Đánh giá:** ⚠️ Thiếu menu quản lý Dịch vụ đi kèm (ServiceItem CRUD) trong `AdminConsole`.

---

#### Luồng 8: Báo cáo & Xuất hóa đơn (AdminConsole)
```
viewReportsAndExport():
  1. BookingService.printRoomUsageStatistics() → [VI PHẠM: Business logic in DAO prints UI]
  2. BookingService.calculateCompletedRevenue(month, year) → tính tổng doanh thu dịch vụ
  3. Option Xuất hóa đơn:
    → Nhập bookingId → BookingService.exportBill()
       → Kiểm tra booking READY
       → Lấy danh sách thiết bị + dịch vụ đã dùng
       → ExportBillUtil.exportBillToFile() → ghi ra file .txt tại out/bills/
```
**Đánh giá:** ✅ Luồng cơ bản đúng. Nhưng `BookingDAO.printRoomUsageStatistics()` đang vi phạm nguyên tắc DAO (xem 3.2).

---

#### Luồng 9: Hệ thống Thông báo (Notification)
```
Trigger gửi:
  approveBooking() → NotificationDAO.insertNotification(userId, message)
  rejectBooking()  → NotificationDAO.insertNotification(userId, message)

Hiển thị khi Employee đăng nhập:
  EmployeeConsole.displayMenu() → NotificationDAO.getUnreadNotifications(userId)
  In toàn bộ thông báo chưa đọc → NotificationDAO.markAllAsRead()
```
**Đánh giá:** ✅ Tính năng nâng cao được triển khai tốt.

---

## 3. Các Lỗi và Vi phạm Nguyên tắc Thiết kế (Design Issues)

### 3.1 LỖI LOGIC SQL NGHIÊM TRỌNG (Critical Bug - Runtime Error)

**Vị trí:** `EquipmentDAO.java` - phương thức `updateEquipment()` (phiên bản cũ trước khi sửa)

> ⚠️ **Lưu ý:** File hiện tại đã được sửa đúng thành `WHERE equipmentId=?`. Hãy kiểm tra lại nếu có bản cũ.

Câu lệnh sai (cần tránh):
```sql
-- SAI: WHERE roomId=? trong bảng equipments!
UPDATE equipments SET equipmentName=?, quantity=?, available=?, status=? WHERE roomId=?
```

Câu lệnh đúng:
```sql
-- ĐÚNG
UPDATE equipments SET equipmentName=?, quantity=?, available=?, status=? WHERE equipmentId=?
```

---

### 3.2 VI PHẠM NGUYÊN TẮC PHÂN TẦNG - Service/DAO chứa Console Output

**Nguyên tắc:** Tầng `Service` chỉ xử lý Business Logic, trả về data (List, Object, boolean) hoặc ném Exception. **Không được** gọi `System.out.println()`. Tầng `DAO` chỉ tương tác CSDL, **không** in UI.

| File | Phương thức vi phạm | Vấn đề |
|---|---|---|
| `RoomService.java` | `displayAllRooms()`, `displayRoomsByName()`, `addRoom()`, `updateRoom()`, `deleteRoom()` | Toàn bộ Service đang làm việc của Presentation |
| `EquipmentService.java` | `displayAllEquipments()`, `addEquipment()`, `updateEquipment()`, `deleteEquipment()` | Tương tự RoomService |
| `ServiceItemService.java` | `displayAllServices()`, `addService()`, `updateService()`, `deleteService()` | Tương tự |
| `SupportStaffService.java` | `printTaskDetails()` | In chi tiết nhiệm vụ – là vai trò của Presentation |
| `BookingService.java` | `printRoomUsageStatistics()` | Delegate thẳng xuống DAO in UI |
| `BookingDAO.java` | `printRoomUsageStatistics()` | DAO đang in trực tiếp ra màn hình |

**Ảnh hưởng:** Không thể tái sử dụng Service cho giao diện khác (Swing, Web). Khó test. Vi phạm Separation of Concerns.

**Hướng sửa:**
```java
// Sai - Service in ra màn hình
public void displayAllRooms() { System.out.println(...); }

// Đúng - Service trả về dữ liệu
public List<Room> getAllRooms() throws Exception {
    return roomDAO.getAllRooms();
}
// Presentation (AdminConsole) sẽ nhận List<Room> và tự in
```

---

### 3.3 UserDAO KHÔNG EXTENDS BaseDAO (Thiếu nhất quán)

**Vị trí:** `UserDAO.java`

`UserDAO` không kế thừa `BaseDAO<User>` mà tự viết lại toàn bộ boilerplate code (try-with-resources, PreparedStatement, Connection). Điều này vi phạm nguyên tắc DRY (Don't Repeat Yourself) và làm mất đi lợi ích của `BaseDAO`.

```java
// UserDAO nên:
public class UserDAO extends BaseDAO<User> {
    @Override
    protected User mapResultSetToObject(ResultSet rs) throws SQLException { ... }
    // Dùng lại executeQuery, executeUpdate, executeQueryForSingleObject từ BaseDAO
}
```

---

### 3.4 Trường `participants` trong Booking không được lưu vào Database

**Vị trí:** `Booking.java` (dòng 15 - comment của tác giả), `BookingDAO.java` (dòng 49 - comment bị comment out)

```java
// Booking.java - có field participants trong Java Model
private int participants;

// BookingDAO.java - nhưng INSERT SQL không có cột participants
String sqlBooking = "INSERT INTO bookings(userId, roomId, startTime, endTime, bookingStatus, preparationStatus) VALUES (?, ?, ?, ?, ?, ?)";
// b.setParticipants(rs.getInt("participants")); // bị comment out
```

**Hậu quả:** Khi `approveBooking()` cần re-check capacity, sẽ dùng `booking.getParticipants()` = 0, làm logic double-check không chính xác:
```java
booking.getParticipants() > 0 ? booking.getParticipants() : 1
// Luôn fallback về 1 nếu chưa lưu participants -> bỏ qua check sức chứa
```

**Hướng sửa:**
1. Thêm cột `participants INT` vào bảng `bookings` trong DB schema.
2. Thêm tham số `participants` vào câu lệnh INSERT.
3. Bỏ comment dòng `b.setParticipants(rs.getInt("participants"))` trong `mapResultSetToObject`.

---

### 3.5 AdminConsole thiếu Menu quản lý Dịch vụ đi kèm (ServiceItem)

**Vị trí:** `AdminConsole.java`

`ServiceItemService` và `ServiceItemDAO` đã được triển khai đầy đủ (CRUD), nhưng `AdminConsole` không có menu để gọi chúng. Admin không thể thêm/sửa/xóa dịch vụ qua giao diện.

**Hướng sửa:** Thêm `case X -> manageServices()` vào `AdminConsole.displayMenu()` và viết phương thức `manageServices()` tương tự `manageRooms()`.

---

### 3.6 `EmployeeConsole` - Option 2 "Yêu cầu dịch vụ phát sinh" chưa hoàn thiện

**Vị trí:** `EmployeeConsole.java` dòng 61

```java
case 1 -> handleBookingWizard(employee);
case 3 -> viewBookingHistory(employee);  // case 2 bị bỏ qua
case 4 -> ProfileConsole.manageProfile(employee);
```

Menu hiển thị 4 lựa chọn nhưng `case 2` (`Yêu cầu dịch vụ văn phòng phát sinh`) không có handler. Người dùng nhập 2 sẽ rơi vào `default` và thấy "Lựa chọn không hợp lệ!".

---

### 3.7 AuthConsole.register() - Không xử lý Exception từ UserService

**Vị trí:** `AuthConsole.java` - phương thức `register()` (dòng 35)

```java
public static void register() throws Exception {
    // ...
    userService.registerEmployee(userName, passWord, department, email, phoneNumber);
    System.out.println("Đăng ký thành công.");
    // Nếu registerEmployee ném Exception (DB lỗi), exception sẽ bubble up ra main()
    // và có thể crash app nếu AppConsole.main() không catch
}
```

`AppConsole.main()` khai báo `throws Exception` → nếu có lỗi DB, toàn bộ app crash. Nên bọc trong `try-catch` ngay tại `AuthConsole.register()`.

---

## 4. Đánh giá so với yêu cầu SRS

### 4.1 Phần bắt buộc (60 điểm)

| Yêu cầu SRS | Trạng thái | Ghi chú |
|---|---|---|
| Đăng ký / Đăng nhập theo phân quyền | ✅ Hoàn chỉnh | Hash mật khẩu, validate email/phone |
| Quản lý hồ sơ cá nhân | ✅ Hoàn chỉnh | ProfileConsole |
| CRUD Phòng họp (Admin) | ✅ Hoàn chỉnh | AdminConsole + RoomService + RoomDAO |
| CRUD Thiết bị di động (Admin) | ✅ Hoàn chỉnh | Nhưng có bug SQL cũ (đã sửa) |
| Employee đặt phòng + chọn thiết bị + dịch vụ | ✅ Hoàn chỉnh | Booking Wizard 6 bước |
| Hệ thống lưu trạng thái PENDING | ✅ Hoàn chỉnh | SQL Transaction |
| Admin duyệt/từ chối + phân công Support Staff | ✅ Hoàn chỉnh | Có double-check xung đột |
| Support Staff cập nhật trạng thái chuẩn bị | ✅ Hoàn chỉnh | PREPARING / READY / MISSING_EQUIPMENT |
| Employee xem lịch họp và trạng thái | ✅ Hoàn chỉnh | viewBookingHistory |
| Employee hủy lịch (chưa duyệt) | ✅ Hoàn chỉnh | Chỉ hủy PENDING |
| Admin tạo tài khoản Staff/Admin | ✅ Hoàn chỉnh | createStaff() |
| Kiểm tra không đặt trùng giờ / xung đột lịch | ✅ Hoàn chỉnh | SQL subquery chống overlap |
| Kiểm tra sức chứa phòng | ✅ Một phần | Có check khi tìm phòng. Nhưng thiếu cột participants trong DB (Lỗi 3.4) |
| Validation đầy đủ (không trống, đúng format) | ✅ Hoàn chỉnh | InputValidation |
| Xử lý ngoại lệ, không crash đột ngột | ✅ Hầu hết | Còn 1 điểm yếu ở register() (Lỗi 3.7) |
| Mã hóa mật khẩu | ✅ Hoàn chỉnh | PasswordUtil (BCrypt) |
| CRUD Dịch vụ đi kèm (ServiceItem) - Admin | ❌ Chưa có UI | Service/DAO đã có nhưng chưa vào menu Admin |

### 4.2 Phần nâng cao (40 điểm)

| Tính năng | Trạng thái | Ghi chú |
|---|---|---|
| **TN1 - Hệ thống thông báo console** | ✅ Hoàn chỉnh | NotificationDAO, gửi khi Approve/Reject, hiển thị khi login |
| **TN2 - Quản lý chi phí & Báo cáo** | ✅ Hoàn chỉnh | Tính doanh thu theo tháng, xuất hóa đơn .txt |
| **TN4 - Phân tích tần suất sử dụng phòng** | ✅ Một phần | printRoomUsageStatistics() có nhưng ở sai tầng |

---

## 5. Các bước cần hoàn thiện (Roadmap)

### 🔴 Ưu tiên cao (Ảnh hưởng chức năng & điểm bắt buộc)

1. **Sửa cột `participants` trong Database:**
   - Thêm cột `participants INT DEFAULT 1` vào bảng `bookings`.
   - Cập nhật INSERT SQL trong `BookingDAO.insertBooking()`.
   - Bỏ comment dòng `b.setParticipants()` trong `mapResultSetToObject()`.

2. **Thêm menu CRUD Dịch vụ đi kèm trong `AdminConsole`:**
   - Thêm `case 7 -> manageServices()` (hoặc số phù hợp).
   - Viết hàm `manageServices()` gọi vào `ServiceItemService`.

3. **Fix `EmployeeConsole` case 2:**
   - Hoàn thiện hoặc xóa option "Yêu cầu dịch vụ phát sinh" khỏi menu.

4. **Tạo file `database.sql`:**
   - DDL đầy đủ cho tất cả bảng: `users`, `rooms`, `equipments`, `services`, `bookings`, `booking_equipments`, `booking_services`, `notifications`.
   - Bao gồm cột `participants` trong `bookings`.
   - Có sẵn dữ liệu seed mẫu để chạy demo.

### 🟡 Ưu tiên trung bình (Cải thiện thiết kế & điểm nâng cao)

5. **Refactor Service Layer - tách UI khỏi Service:**
   - `RoomService`: Thay `displayAllRooms()` bằng `getAllRooms()` trả về `List<Room>`.
   - `EquipmentService`: Tương tự.
   - `ServiceItemService`: Tương tự.
   - Phần print console chuyển về `AdminConsole`/`EmployeeConsole`.

6. **Refactor `UserDAO` extends `BaseDAO<User>`:**
   - Xóa boilerplate code thủ công, dùng lại `executeQuery`, `executeUpdate`, `executeQueryForSingleObject`.

7. **Fix `AuthConsole.register()` - bọc try-catch:**
   - Ngăn exception từ UserService làm crash `AppConsole.main()`.

8. **`BookingDAO.printRoomUsageStatistics()` - chuyển về Service/Presentation:**
   - DAO trả về `List<Map<String, Object>>` hoặc tạo DTO riêng.
   - Service nhận và đẩy lên Presentation để in.

### 🟢 Ưu tiên thấp (Tính năng nâng cao bổ sung)

9. **Tính năng 3: Đánh giá sau cuộc họp (Nếu muốn thêm điểm)**
   - Model `Review` (rating 1-5, comment, bookingId).
   - Employee đánh giá sau khi preparationStatus = READY.
   - Admin xem tổng hợp.

10. **Tính năng 5: Tích hợp lịch cá nhân (Nếu muốn thêm điểm)**
    - Nhân viên khai báo khung giờ bận.
    - Wizard đặt phòng tự cảnh báo nếu trùng lịch cá nhân.

---

## 6. Điểm mạnh của thiết kế hiện tại

- ✅ **BaseDAO Generic (Template Method Pattern):** Loại bỏ boilerplate JDBC, tái sử dụng tốt, chuẩn OOP.
- ✅ **SQL Transaction trong BookingDAO:** Đảm bảo tính toàn vẹn khi tạo Booking (atomic insert).
- ✅ **Double-check xung đột khi duyệt:** Ngăn race condition Admin duyệt 2 booking trùng phòng.
- ✅ **Validation từ InputValidation:** Email, phone, số nguyên, không để trống – tập trung tại 1 chỗ.
- ✅ **BCrypt Password Hashing:** Bảo mật đúng chuẩn, không lưu plain text.
- ✅ **Notification System:** Tích hợp gọn vào luồng duyệt/từ chối, tự động hiển thị khi đăng nhập.
- ✅ **ExportBillUtil:** File xuất hóa đơn .txt được tổ chức gọn trong util, không lẫn vào logic.
- ✅ **Enum cho trạng thái:** Tránh magic string, type-safe, dễ đọc.
