# PROJECT_ANALYSIS.md
# Hệ thống Quản lý Đặt Phòng Họp & Dịch vụ Văn phòng
**Mã dự án:** PRJ-MEETING-JAVA-05 | **Môn:** Java Advanced | **Cập nhật:** 2026-04-13

---

## MỤC LỤC

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Kiến trúc hệ thống](#2-kiến-trúc-hệ-thống)
3. [Cấu trúc package chi tiết](#3-cấu-trúc-package-chi-tiết)
4. [Mô hình dữ liệu (Database Schema)](#4-mô-hình-dữ-liệu-database-schema)
5. [Chức năng theo từng Actor](#5-chức-năng-theo-từng-actor)
6. [Luồng hoạt động chi tiết](#6-luồng-hoạt-động-chi-tiết)
7. [Kiến thức Java cốt lõi](#7-kiến-thức-java-cốt-lõi)
8. [Design Patterns](#8-design-patterns)
9. [SOLID Principles](#9-solid-principles)
10. [Bảo mật & Xử lý ngoại lệ](#10-bảo-mật--xử-lý-ngoại-lệ)
11. [Điểm nổi bật kỹ thuật](#11-điểm-nổi-bật-kỹ-thuật)
12. [Phân loại kiến thức: Cơ bản → Nâng cao](#12-phân-loại-kiến-thức-cơ-bản--nâng-cao)
13. [Điểm còn hạn chế & Hướng phát triển](#13-điểm-còn-hạn-chế--hướng-phát-triển)
14. [Tóm tắt nhanh cho báo cáo/bảo vệ](#14-tóm-tắt-nhanh-cho-báo-cáobảo-vệ)

---

## 1. Tổng quan dự án

### 1.1 Mục tiêu

Xây dựng ứng dụng **console Java** quản lý toàn bộ quy trình đặt phòng họp trong một tòa văn phòng, bao gồm:

- Nhân viên (**EMPLOYEE**) tự đặt phòng, chọn thiết bị di động, chọn dịch vụ ăn uống đi kèm
- Quản trị viên (**ADMIN**) duyệt/từ chối yêu cầu, phân công nhân viên hỗ trợ, xem thống kê, xuất hóa đơn
- Nhân viên hỗ trợ (**SUPPORT_STAFF**) nhận nhiệm vụ chuẩn bị phòng và cập nhật trạng thái

### 1.2 Thông tin kỹ thuật

| Mục | Chi tiết |
|---|---|
| **Ngôn ngữ** | Java (JDK 17+, dùng switch expression, text block) |
| **Database** | MySQL — schema: `meeting_manager` |
| **JDBC Driver** | MySQL Connector/J |
| **Bảo mật password** | jBCrypt (`org.mindrot.jbcrypt`) — cost factor 12 |
| **Kiến trúc** | 3-Tier: Presentation → Service → DAO |
| **IDE** | IntelliJ IDEA |
| **Cấu hình DB** | `src/db.properties` (tách khỏi source code) |

### 1.3 3 Actor của hệ thống

```
┌─────────────────────────────────────────────────────────┐
│                    HỆ THỐNG                             │
│                                                         │
│  EMPLOYEE          ADMIN              SUPPORT_STAFF      │
│  - Đặt phòng       - Quản lý users    - Nhận nhiệm vụ   │
│  - Xem lịch sử     - Quản lý phòng    - Cập nhật trạng  │
│  - Sửa hồ sơ       - Quản lý TB       thái chuẩn bị     │
│                    - Duyệt booking                       │
│                    - Báo cáo/Hóa đơn                     │
│                    - Quản lý nhân sự                     │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Kiến trúc hệ thống

### 2.1 Mô hình 3-Tier

```
┌─────────────────────────────────────────────────────────────┐
│  PRESENTATION LAYER (Console UI)                            │
│                                                             │
│  AppConsole (Entry point + main loop)                       │
│       ↓                                                     │
│  AuthConsole (Login / Register)                             │
│       ↓ (theo Role)                                         │
│  ┌──────────────┬──────────────┬─────────────────────────┐  │
│  │EmployeeConsole│ AdminConsole │ SupportStaffConsole     │  │
│  │  (thin router)│ (thin router)│ (thin router)           │  │
│  └──────┬───────┴──────┬───────┴──────────┬──────────────┘  │
│         ↓              ↓                  ↓                 │
│  employee/         admin/             staff/                │
│  BookingWizard     UserManagement     TaskManagement        │
│                    RoomManagement                           │
│                    EquipmentManagement                      │
│                    ServiceManagement                        │
│                    BookingManagement                        │
│                    ReportManagement                         │
│  ProfileConsole (dùng chung 3 role)                         │
├─────────────────────────────────────────────────────────────┤
│  SERVICE LAYER (Business Logic)                             │
│                                                             │
│  IUserService       → UserService                           │
│  IBookingService    → BookingService                        │
│  IRoomService       → RoomService                           │
│  IEquipmentService  → EquipmentService                      │
│  IServiceItemService → ServiceItemService                   │
│  ISupportStaffService → SupportStaffService                 │
│  INotificationService → NotificationService                 │
├─────────────────────────────────────────────────────────────┤
│  DATA ACCESS LAYER (DAO + JDBC)                             │
│                                                             │
│  BaseDAO<T> (Template Method Pattern)                       │
│       ↑ extends                                             │
│  UserDAO   RoomDAO   EquipmentDAO   ServiceItemDAO          │
│  BookingDAO   NotificationDAO                               │
└─────────────────────────────────────────────────────────────┘
                          ↓ JDBC (PreparedStatement)
                    MySQL Database
                  (schema: meeting_manager)
```

### 2.2 Nguyên tắc phụ thuộc giữa các tầng

```
Presentation  →  IXxxService (interface)  ←  XxxService (implementation)
                                                  ↓
                                            XxxDAO (concrete)
                                                  ↓
                                           MySQL (via JDBC)
```

- **Presentation** chỉ biết Interface → tuân thủ DIP
- **Service** không biết SQL, chỉ biết DAO
- **DAO** không biết UI, chỉ xử lý DB

---

## 3. Cấu trúc package chi tiết

```
src/
├── db.properties                    ← DB credentials (không commit lên Git)
│
├── dao/
│   ├── BaseDAO<T>                   ← Abstract class: Template Method Pattern
│   │                                  executeQuery(), executeUpdate(),
│   │                                  executeQueryForSingleObject()
│   │                                  abstract mapResultSetToObject(ResultSet)
│   ├── UserDAO                      ← getAllUsers, getUserById, getUserByUsername,
│   │                                  isUsernameExist, insertUser, updateUserProfile,
│   │                                  updateUserByAdmin, deleteUser, getSupportStaffs
│   ├── RoomDAO                      ← getAllRooms, getRoomById, getRoomsByName,
│   │                                  insertRoom, updateRoom, deleteRoom
│   ├── EquipmentDAO                 ← getAllEquipments, getEquipmentById,
│   │                                  insertEquipment, updateEquipment, deleteEquipment
│   ├── ServiceItemDAO               ← getAllServices, getServiceById,
│   │                                  insertService, updateService, deleteService
│   ├── BookingDAO                   ← Phức tạp nhất:
│   │                                  · insertBooking() — JDBC Transaction + Batch
│   │                                  · getPendingBookings()
│   │                                  · getAvailableRooms() — overlap detection SQL
│   │                                  · approveAndAssign(), rejectBooking()
│   │                                  · getBookingsByUserId() — lịch sử nhân viên
│   │                                  · getTasksByStaffId() — nhiệm vụ staff
│   │                                  · updatePreparationStatus()
│   │                                  · getEquipmentsByBookingId() → BookingEquipmentDetail
│   │                                  · getServicesByBookingId() → BookingServiceDetail
│   │                                  · getRoomUsageStatistics() → Map<Room, Integer>
│   │                                  · calculateCompletedRevenue(month, year)
│   │                                  · exportBill() → ExportBillUtil
│   └── NotificationDAO              ← insertNotification, getUnreadNotifications,
│                                      markAllAsRead
│
├── model/
│   ├── User                         ← userId, username, password, department,
│   │                                  phoneNumber, contact, Role role
│   ├── Room                         ← roomId, roomName, capacity, location, fixedDevice
│   ├── Equipment                    ← equipmentId, equipmentName, quantity,
│   │                                  available, EquipmentStatus status
│   ├── ServiceItem                  ← serviceId, serviceName, unit, price, description
│   ├── Booking                      ← bookingId, userId, roomId, supportStaffId(nullable),
│   │                                  startTime, endTime, participants,
│   │                                  BookingStatus, PreparationStatus, createdAt
│   ├── BookingDetailEquipment       ← bookingId, equipmentId, quantity (join-table)
│   ├── BookingDetailService         ← bookingId, serviceId, quantity (join-table)
│   ├── Notification                 ← id, userId, message, isRead, createdAt
│   ├── Enum/
│   │   ├── Role                     ← EMPLOYEE, SUPPORT_STAFF, ADMIN
│   │   ├── BookingStatus            ← PENDING, APPROVED, REJECTED, CANCELED
│   │   ├── PreparationStatus        ← PENDING, PREPARING, READY, MISSING_EQUIPMENT
│   │   └── EquipmentStatus          ← ACTIVE, MAINTENANCE, BROKEN
│   └── dto/
│       ├── BookingServiceDetail     ← serviceId, serviceName, unit, price,
│       │                              orderedQuantity, getSubTotal()
│       └── BookingEquipmentDetail   ← equipmentId, equipmentName, borrowedQuantity
│
├── service/
│   ├── IUserService                 ← checkUsername, registerEmployee, createStaffAdmin,
│   │                                  login, updateProfile, getSupportStaffs,
│   │                                  getAllUsers, deleteUser, updateUserByAdmin, getUserById
│   ├── UserService                  ← implements IUserService
│   │                                  · login(): BCrypt.checkpw()
│   │                                  · registerEmployee/createStaffAdmin(): BCrypt.hashpw()
│   ├── IBookingService              ← getAvailableRooms, createBooking, getPendingBookings,
│   │                                  approveBooking (race-condition check),
│   │                                  rejectBooking, getBookingsByUserId,
│   │                                  updatePreparationStatus, exportBill,
│   │                                  getRoomUsageStatistics, calculateCompletedRevenue
│   ├── BookingService               ← implements IBookingService (gọi BookingDAO + NotificationService)
│   ├── IRoomService                 ← getAllRooms, getRoomsByName, addRoom,
│   │                                  updateRoom, deleteRoom, getRoomById
│   ├── RoomService                  ← implements IRoomService
│   ├── IEquipmentService            ← getAllEquipments, addEquipment,
│   │                                  updateEquipment, deleteEquipment
│   ├── EquipmentService             ← implements IEquipmentService
│   ├── IServiceItemService          ← getAllServices, addService,
│   │                                  updateService, deleteService
│   ├── ServiceItemService           ← implements IServiceItemService
│   ├── ISupportStaffService         ← getTasksByStaffId
│   ├── SupportStaffService          ← implements ISupportStaffService
│   ├── INotificationService         ← insertNotification, getUnreadNotifications,
│   │                                  markAllAsRead
│   └── NotificationService          ← implements INotificationService
│
├── presentation/
│   ├── AppConsole                   ← main(), menuLogin(), fatal error handler,
│   │                                  while(true) main loop, role-based routing
│   ├── AuthConsole                  ← login(), register(),
│   │                                  inputUsername() (loop check unique),
│   │                                  inputConfirmPassword() (confirm match)
│   ├── ProfileConsole               ← manageProfile(User): showProfile, updateProfile
│   │                                  (dùng inputStringAllowEmpty để skip trường)
│   ├── EmployeeConsole              ← displayMenu(User employee), showUnreadNotifications()
│   ├── AdminConsole                 ← displayMenu(User admin) — thin router 7 options
│   ├── SupportStaffConsole          ← displayMenu(User support) — thin router 1 option
│   │
│   ├── admin/
│   │   ├── UserManagement           ← CRUD người dùng:
│   │   │                              · listUsers() — printf table
│   │   │                              · createUser() — chọn role, check unique username
│   │   │                              · updateUser() — sửa dept/contact/phone/role
│   │   │                              · deleteUser() — bảo vệ không xóa chính mình
│   │   ├── RoomManagement           ← CRUD phòng họp + tìm kiếm theo tên
│   │   ├── EquipmentManagement      ← CRUD thiết bị di động
│   │   ├── ServiceManagement        ← CRUD dịch vụ đi kèm
│   │   ├── BookingManagement        ← Xem PENDING bookings, duyệt (assign staff),
│   │   │                              từ chối booking
│   │   └── ReportManagement         ← Thống kê tần suất phòng, doanh thu tháng,
│   │                                  xuất hóa đơn ra file .txt
│   │
│   ├── employee/
│   │   └── BookingWizard            ← 8-bước wizard đặt phòng:
│   │                                  1. inputDateTimeRange (LocalDateTime parse)
│   │                                  2. Số người tham gia (inputPositiveInt)
│   │                                  3. Chọn phòng còn trống (overlap detection)
│   │                                  4. Xác nhận phòng từ danh sách
│   │                                  5. Chọn thiết bị + số lượng (validate available)
│   │                                  6. Chọn dịch vụ đi kèm
│   │                                  7. Xem lại tổng quan + tính chi phí
│   │                                  8. Confirm → createBooking (Transaction)
│   │                                  + viewHistory(employee): xem lịch sử đặt phòng
│   │
│   └── staff/
│       └── TaskManagement           ← manage(User support):
│                                      · Xem nhiệm vụ được phân công
│                                      · Xem chi tiết: thiết bị cần chuẩn bị,
│                                        dịch vụ cần chuẩn bị
│                                      · Cập nhật trạng thái: PREPARING/READY/MISSING_EQUIPMENT
│                                      · Validate ownership (chỉ thấy task của mình)
│
├── util/
│   ├── DatabaseConnection           ← getConnection(): load db.properties từ classpath,
│   │                                  DriverManager.getConnection()
│   ├── InputValidation              ← inputString(), inputStringAllowEmpty(),
│   │                                  inputInt(), inputPositiveInt(),
│   │                                  inputEmail() (regex), inputPhoneNumber() (regex VN),
│   │                                  inputPhoneNumberAllowEmpty()
│   ├── PasswordUtil                 ← hashPassword(plain) → BCrypt.hashpw(cost=12)
│   │                                  checkPassword(plain, hash) → BCrypt.checkpw()
│   └── ExportBillUtil               ← exportBillToFile(): tạo out/bills/, ghi .txt
│
└── exception/
    └── InvalidRegisterException     ← Custom exception cho luồng đăng ký
```

---

## 4. Mô hình dữ liệu (Database Schema)

### 4.1 Sơ đồ bảng

```
users
  userId (PK, AUTO_INCREMENT)
  username (UNIQUE, NOT NULL)
  password (VARCHAR 255 — BCrypt hash)
  department
  roleUser ENUM('EMPLOYEE','SUPPORT_STAFF','ADMIN')
  contact (email)
  phoneNumber
  createdAt (TIMESTAMP DEFAULT NOW)

rooms
  roomId (PK, AUTO_INCREMENT)
  roomName (UNIQUE, NOT NULL)
  capacity (INT)
  location
  fixedDevice (TEXT — thiết bị cố định)

equipments
  equipmentId (PK, AUTO_INCREMENT)
  equipmentName
  quantity (tổng số)
  available (số còn có thể mượn)
  status ENUM('ACTIVE','MAINTENANCE','BROKEN')

services
  serviceId (PK, AUTO_INCREMENT)
  serviceName
  unit (Chai/Người/Set/Lần...)
  price (DOUBLE)
  description (TEXT)

bookings
  bookingId (PK, AUTO_INCREMENT)
  userId (FK → users)
  roomId (FK → rooms)
  supportStaffId (FK → users, NULLABLE — chỉ có sau khi approved)
  startTime (DATETIME)
  endTime (DATETIME)
  participants (INT)
  bookingStatus ENUM('PENDING','APPROVED','REJECTED','CANCELED')
  preparationStatus ENUM('PENDING','PREPARING','READY','MISSING_EQUIPMENT')
  createdAt (TIMESTAMP)

booking_equipments
  id (PK)
  bookingId (FK → bookings ON DELETE CASCADE)
  equipmentId (FK → equipments)
  quantity

booking_services
  id (PK)
  bookingId (FK → bookings ON DELETE CASCADE)
  serviceId (FK → services)
  quantity

notifications
  id (PK)
  userId (FK → users ON DELETE CASCADE)
  message (TEXT)
  isRead (BOOLEAN DEFAULT FALSE)
  createdAt (TIMESTAMP)
```

### 4.2 Quan hệ chính

```
users  1──∞  bookings (với vai trò employee, qua userId)
users  1──∞  bookings (với vai trò supportStaff, qua supportStaffId)
rooms  1──∞  bookings
bookings  1──∞  booking_equipments  ∞──1  equipments
bookings  1──∞  booking_services    ∞──1  services
users  1──∞  notifications
```

### 4.3 Seed data mặc định

- 4 tài khoản: `admin1` (ADMIN), `support_linh` (SUPPORT_STAFF), `employee_dat`, `employee_mai` (EMPLOYEE)
- Password mặc định: `123456` — BCrypt hash hợp lệ 60 ký tự, cost 12
- 3 phòng họp, 5 thiết bị, 4 dịch vụ

---

## 5. Chức năng theo từng Actor

### 5.1 EMPLOYEE

| # | Chức năng | Module |
|---|---|---|
| 1 | Đăng nhập / Đăng ký (tự đăng ký role EMPLOYEE) | `AuthConsole` |
| 2 | Đặt phòng họp 8 bước (wizard) | `BookingWizard.start()` |
| 3 | Xem lịch sử đặt phòng của mình | `BookingWizard.viewHistory()` |
| 4 | Nhận thông báo từ hệ thống khi vừa đăng nhập | `EmployeeConsole.showUnreadNotifications()` |
| 5 | Xem & cập nhật hồ sơ cá nhân (dept, email, phone) | `ProfileConsole` |

### 5.2 ADMIN

| # | Chức năng | Module |
|---|---|---|
| 1 | **Quản lý người dùng** — Xem danh sách, Thêm ADMIN/SUPPORT_STAFF, Sửa thông tin+role, Xóa (bảo vệ không tự xóa) | `UserManagement` |
| 2 | **Quản lý phòng họp** — CRUD + tìm kiếm theo tên | `RoomManagement` |
| 3 | **Quản lý thiết bị di động** — CRUD (số lượng, trạng thái) | `EquipmentManagement` |
| 4 | **Quản lý đặt phòng** — Xem PENDING, Duyệt (assign staff), Từ chối | `BookingManagement` |
| 5 | **Quản lý dịch vụ đi kèm** — CRUD (tên, đơn vị, giá, mô tả) | `ServiceManagement` |
| 6 | **Báo cáo & Hóa đơn** — Thống kê tần suất phòng, doanh thu tháng, xuất hóa đơn .txt | `ReportManagement` |
| 7 | Xem & cập nhật hồ sơ cá nhân | `ProfileConsole` |

### 5.3 SUPPORT_STAFF

| # | Chức năng | Module |
|---|---|---|
| 1 | Xem danh sách nhiệm vụ được phân công (APPROVED booking) | `TaskManagement` |
| 2 | Xem chi tiết nhiệm vụ: thiết bị cần chuẩn bị, dịch vụ cần làm | `TaskManagement.printTaskDetail()` |
| 3 | Cập nhật trạng thái chuẩn bị: PREPARING / READY / MISSING_EQUIPMENT | `TaskManagement.updateStatus()` |
| 4 | Xem & cập nhật hồ sơ cá nhân (qua ProfileConsole — theo thiết kế, bổ sung sau) | `ProfileConsole` |

---

## 6. Luồng hoạt động chi tiết

### Luồng 1: Khởi động & Đăng nhập

```
AppConsole.main()
  ├── [try-catch FATAL] bọc toàn bộ app → System.exit(1) nếu crash
  └── while(true):
       ├── menuLogin() → hiển thị 3 option
       ├── case 1: AuthConsole.login()
       │     ├── InputValidation.inputString() × 2
       │     ├── UserService.login(username, password)
       │     │     ├── UserDAO.getUserByUsername() → User object (có hash)
       │     │     └── PasswordUtil.checkPassword(plain, hash)
       │     │           └── BCrypt.checkpw() → true/false
       │     └── Return User (hoặc null nếu sai)
       ├── case 2: AuthConsole.register()
       │     ├── inputUsername() — loop check unique via UserService.checkUsername()
       │     ├── inputConfirmPassword() — loop cho đến khi 2 lần nhập khớp
       │     ├── inputEmail() + inputPhoneNumber() (regex)
       │     └── UserService.registerEmployee() → BCrypt.hashpw(cost=12) → insert DB
       └── switch(loggedUser.getRole()):
            EMPLOYEE      → EmployeeConsole.displayMenu(user)
            ADMIN         → AdminConsole.displayMenu(user)
            SUPPORT_STAFF → SupportStaffConsole.displayMenu(user)
```

---

### Luồng 2: Đặt phòng (BookingWizard — 8 bước)

```
BookingWizard.start(employee)
  │
  ├── [Bước 1] inputDateTimeRange()
  │     ├── LocalDate.parse(input, "yyyy-MM-dd") → DateTimeParseException nếu sai
  │     ├── LocalTime.parse(input, "HH:mm")
  │     └── LocalDateTime.of(date, startTime), LocalDateTime.of(date, endTime)
  │
  ├── [Bước 2] inputPositiveInt() → số người tham gia (> 0)
  │
  ├── [Bước 3] bookingService.getAvailableRooms(start, end, capacity)
  │     ├── Validate: start > now, end > start (Service layer throw Exception nếu sai)
  │     └── BookingDAO.getAvailableRooms():
  │           SQL: SELECT rooms WHERE capacity >= ?
  │                AND roomId NOT IN (
  │                  SELECT roomId FROM bookings
  │                  WHERE bookingStatus IN ('PENDING','APPROVED')
  │                  AND startTime < ?endTime AND endTime > ?startTime
  │                )
  │           ← Đây là overlap detection chuẩn kỹ thuật
  │
  ├── [Bước 4] selectRoom()
  │     └── Validate: roomId phải nằm trong danh sách phòng vừa lấy từ DB
  │
  ├── [Bước 5] selectEquipments()
  │     ├── equipmentService.getAllEquipments() — gọi 1 lần duy nhất
  │     └── while(true) loop:
  │           ├── Validate ID có trong danh sách thực tế
  │           └── Validate qty <= equipment.getAvailable()
  │
  ├── [Bước 6] selectServices()
  │     ├── serviceService.getAllServices() — gọi 1 lần
  │     └── while(true) loop: validate ID
  │
  ├── [Bước 7] printReview() + calculateTotalServiceCost()
  │     └── 1 lần gọi getAllServices() → Map<serviceId, price> → Stream.sum()
  │         (tránh N+1 query)
  │
  └── [Bước 8] Confirm → bookingService.createBooking()
        └── BookingDAO.insertBooking() [JDBC TRANSACTION]:
              conn.setAutoCommit(false)
              try:
                INSERT INTO bookings ... → RETURN_GENERATED_KEYS → newBookingId
                INSERT INTO booking_equipments (BATCH: addBatch + executeBatch)
                INSERT INTO booking_services   (BATCH: addBatch + executeBatch)
                conn.commit()
              catch SQLException:
                conn.rollback()   ← Không bao giờ để dữ liệu rác
                throw e
              finally:
                conn.setAutoCommit(true), conn.close()
```

---

### Luồng 3: Admin duyệt Booking

```
AdminConsole → BookingManagement.manage()
  ├── bookingService.getPendingBookings()
  │     └── BookingDAO: SELECT * WHERE bookingStatus = 'PENDING'
  ├── Hiển thị bảng printf với ID, UserID, RoomID, StartTime, EndTime
  ├── Nhập bookingId + action (1=Duyệt / 2=Từ chối)
  │
  ├── [Duyệt] handleApprove(bookingId):
  │     ├── userService.getSupportStaffs() → hiển thị danh sách
  │     ├── Nhập staffId (validate có trong danh sách)
  │     └── bookingService.approveBooking(bookingId, staffId):
  │           ├── [Check 1] bookingDAO.getBookingById() → null? → throw
  │           ├── [Check 2] booking.status == PENDING? → Nếu không → throw
  │           ├── [Check 3] Race-condition double-check:
  │           │     bookingDAO.getAvailableRooms(start, end, capacity)
  │           │     → Phòng vẫn trong danh sách? → Nếu không → throw "Xung đột lịch"
  │           ├── bookingDAO.approveAndAssign(bookingId, staffId):
  │           │     UPDATE bookings SET bookingStatus='APPROVED',
  │           │                        supportStaffId=?,
  │           │                        preparationStatus='PREPARING'
  │           └── notificationService.insertNotification(userId, message)
  │
  └── [Từ chối] handleReject(bookingId):
        └── bookingService.rejectBooking(bookingId):
              UPDATE bookings SET bookingStatus='REJECTED'
              notificationService.insertNotification(userId, message)
```

---

### Luồng 4: Support Staff xử lý nhiệm vụ

```
SupportStaffConsole → TaskManagement.manage(support)
  ├── supportService.getTasksByStaffId(staffId)
  │     SQL: SELECT * FROM bookings
  │          WHERE supportStaffId = ?
  │          AND bookingStatus = 'APPROVED'
  │          AND preparationStatus IN ('PENDING','PREPARING','MISSING_EQUIPMENT')
  │
  ├── Hiển thị danh sách nhiệm vụ
  ├── Nhập bookingId muốn xem chi tiết
  │     └── Validate ownership: task.supportStaffId == loggedUser.userId
  │
  ├── printTaskDetail(bookingId):
  │     ├── getBookingInfo() — header
  │     ├── getTaskEquipments() → List<BookingEquipmentDetail>
  │     └── getTaskServices()  → List<BookingServiceDetail>
  │
  └── updateStatus():
        Nhập 1/2/3 → UPDATE bookings SET preparationStatus = ?
        1 → PREPARING | 2 → READY | 3 → MISSING_EQUIPMENT
```

---

### Luồng 5: Báo cáo & Xuất hóa đơn

```
AdminConsole → ReportManagement.manage()
  ├── printRoomUsageStats():
  │     bookingService.getRoomUsageStatistics()
  │     → BookingDAO: SELECT roomId, COUNT(*) GROUP BY roomId
  │                   (chỉ APPROVED bookings)
  │     → Map<Room, Integer> (dùng LinkedHashMap giữ thứ tự)
  │
  ├── printMonthlyRevenue():
  │     bookingService.calculateCompletedRevenue(month, year)
  │     → SQL JOIN booking_services + services
  │       WHERE preparationStatus='READY' AND MONTH/YEAR filter
  │     → Tổng doanh thu dịch vụ
  │
  └── exportBillMenu() → bookingService.exportBill(bookingId):
        ├── [Check] booking != null
        ├── [Check] preparationStatus == READY
        ├── getEquipmentsByBookingId() → List<BookingEquipmentDetail>
        ├── getServicesByBookingId()   → List<BookingServiceDetail>
        ├── totalCost = stream.mapToDouble(getSubTotal).sum()
        └── ExportBillUtil.exportBillToFile():
              · Tạo thư mục out/bills/ (mkdirs)
              · Tên file: Bill_Booking_{id}_{timestamp}.txt
              · FileWriter (try-with-resources)
              · Ghi: header, room info, equipment list, service list, total cost
```

---

### Luồng 6: Thông báo

```
[Khi Admin approve hoặc reject]:
  BookingService → notificationService.insertNotification(userId, message)
  → NotificationDAO: INSERT INTO notifications (userId, message)

[Khi Employee đăng nhập]:
  EmployeeConsole.showUnreadNotifications(userId):
    → notificationService.getUnreadNotifications(userId)
    → SELECT * WHERE userId=? AND isRead=FALSE ORDER BY createdAt DESC
    → Hiển thị nếu có
    → notificationService.markAllAsRead(userId)
    → UPDATE notifications SET isRead=TRUE WHERE userId=? AND isRead=FALSE
```

---

## 7. Kiến thức Java cốt lõi

### 7.1 OOP (4 trụ cột)

#### Encapsulation
```java
// Tất cả model fields đều private, truy cập qua getter/setter
public class Booking {
    private int bookingId;
    private BookingStatus bookingStatus;
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus s) { this.bookingStatus = s; }
}
```

#### Inheritance (Kế thừa)
```java
// 6 DAO kế thừa BaseDAO<T>, tái sử dụng toàn bộ JDBC boilerplate
public class BookingDAO extends BaseDAO<Booking> {
    @Override
    protected Booking mapResultSetToObject(ResultSet rs) throws SQLException {
        // Chỉ cần viết phần mapping riêng — không viết lại connect/prepare/close
    }
}
```

#### Polymorphism (Đa hình)
```java
// Presentation dùng interface — có thể đổi implementation không cần sửa UI
private static final IBookingService bookingService = new BookingService();
// Sau này: = new CachedBookingService(); — không đụng code BookingWizard
```

#### Abstraction
```java
// BaseDAO định nghĩa CONTRACT, ép buộc subclass implement mapping
public abstract class BaseDAO<T> {
    protected abstract T mapResultSetToObject(ResultSet rs) throws SQLException;
}
```

### 7.2 Generics

```java
public abstract class BaseDAO<T> {
    protected List<T> executeQuery(String sql, Object... params) throws SQLException {
        List<T> list = new ArrayList<>();
        // ...
        list.add(mapResultSetToObject(rs)); // T xác định lúc compile
        return list;
    }
}

BookingDAO   extends BaseDAO<Booking>     // T = Booking
RoomDAO      extends BaseDAO<Room>        // T = Room
UserDAO      extends BaseDAO<User>        // T = User
EquipmentDAO extends BaseDAO<Equipment>   // T = Equipment
```

### 7.3 Enum

```java
public enum BookingStatus   { PENDING, APPROVED, REJECTED, CANCELED }
public enum PreparationStatus { PENDING, PREPARING, READY, MISSING_EQUIPMENT }
public enum EquipmentStatus { ACTIVE, MAINTENANCE, BROKEN }
public enum Role            { EMPLOYEE, SUPPORT_STAFF, ADMIN }

// Map từ DB String → Enum
b.setBookingStatus(BookingStatus.valueOf(rs.getString("bookingStatus")));

// Lưu vào DB Enum → String
ps.setString(6, b.getBookingStatus().name());
```

### 7.4 Collections & Stream API

```java
// ArrayList — danh sách có thứ tự
List<Room> availableRooms = new ArrayList<>();

// LinkedHashMap — giữ thứ tự insert (dùng trong báo cáo phòng)
Map<Room, Integer> stats = new LinkedHashMap<>();

// Stream + Collectors.toMap — build Map một lần để tính giá
Map<Integer, Double> priceMap = serviceItemDAO.getAllServices().stream()
        .collect(Collectors.toMap(ServiceItem::getServiceId, ServiceItem::getPrice));

// Stream sum — tính tổng chi phí dịch vụ
double totalCost = svList.stream()
        .mapToDouble(BookingServiceDetail::getSubTotal)
        .sum();

// Stream filter + findFirst — tìm equipment theo ID
Equipment selected = equipments.stream()
        .filter(e -> e.getEquipmentId() == eqId)
        .findFirst().orElse(null);

// anyMatch — kiểm tra staffId hợp lệ
boolean valid = staffList.stream().anyMatch(s -> s.getUserId() == staffId);

// forEach — duyệt và in danh sách
unread.forEach(n -> System.out.println(" - " + n.getMessage()));
```

### 7.5 JDBC nâng cao

#### PreparedStatement (chống SQL Injection)
```java
String sql = "SELECT * FROM users WHERE username = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, username); // Driver escape tự động, không qua String concat
```

#### JDBC Transaction
```java
conn.setAutoCommit(false);
try {
    // INSERT bookings, INSERT booking_equipments, INSERT booking_services
    conn.commit();         // Tất cả thành công
} catch (SQLException e) {
    conn.rollback();       // Bất kỳ lỗi → hủy hết
    throw e;
} finally {
    conn.setAutoCommit(true);
    conn.close();
}
```

#### Batch Execution
```java
for (BookingDetailEquipment eq : eqList) {
    psEq.setInt(1, newBookingId);
    psEq.setInt(2, eq.getEquipmentId());
    psEq.setInt(3, eq.getQuantity());
    psEq.addBatch();    // Gom vào batch
}
psEq.executeBatch();    // Gửi 1 lần → N lần gọi SQL đơn giản
```

#### getGeneratedKeys()
```java
psBooking = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
psBooking.executeUpdate();
ResultSet keys = psBooking.getGeneratedKeys();
if (keys.next()) {
    int newBookingId = keys.getInt(1); // Lấy auto_increment ID vừa tạo
}
```

#### Try-with-resources
```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    // Resources tự động close khi ra khỏi block (kể cả exception)
}
```

#### Null-safe cho nullable INT column
```java
int supportStaffId = rs.getInt("supportStaffId");
if (!rs.wasNull()) {
    b.setSupportStaffId(supportStaffId); // Java Integer wrapper nhận null
}
```

### 7.6 Date/Time API (Java 8 — java.time)

```java
LocalDate date = LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
LocalTime time = LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm"));
LocalDateTime start = LocalDateTime.of(date, time);

// Validate
if (start.isBefore(LocalDateTime.now())) throw new Exception("Thời gian đã qua");
if (end.isBefore(start) || end.isEqual(start)) throw new Exception("Thời gian không hợp lệ");

// Chuyển đổi JDBC ↔ Java
Timestamp.valueOf(startDateTime)                    // Java → JDBC
rs.getTimestamp("startTime").toLocalDateTime()      // JDBC → Java
```

### 7.7 Varargs

```java
// BaseDAO nhận bất kỳ số tham số nào, setObject tự xử lý kiểu
protected boolean executeUpdate(String sql, Object... params) throws SQLException {
    for (int i = 0; i < params.length; i++) {
        ps.setObject(i + 1, params[i]);
    }
}

// Caller:
executeUpdate("UPDATE ... WHERE id = ?", bookingId);
executeUpdate("INSERT VALUES (?, ?, ?)", name, qty, status.name());
```

### 7.8 BCrypt Password Security

```java
// Hash khi đăng ký/tạo tài khoản — one-way, cost=12
String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

// Verify khi đăng nhập — không so sánh plain text bao giờ
boolean isMatch = BCrypt.checkpw(inputPassword, storedHash);
```

### 7.9 Properties File

```java
Properties p = new Properties();
try (InputStream is = DatabaseConnection.class.getClassLoader()
        .getResourceAsStream("db.properties")) {
    p.load(is);
}
String url  = p.getProperty("db.url");
String user = p.getProperty("db.user");
String pass = p.getProperty("db.password");
```

### 7.10 Custom Exception

```java
public class InvalidRegisterException extends Exception {
    public InvalidRegisterException(String message) { super(message); }
}

// Sử dụng — caller biết đây là lỗi đăng ký, không phải lỗi system
} catch (InvalidRegisterException e) {
    System.out.println("Lỗi đăng ký: " + e.getMessage());
}
```

### 7.11 Java I/O — Xuất hóa đơn

```java
// Tạo thư mục nếu chưa có
new File("out/bills").mkdirs();

// FileWriter với try-with-resources
try (FileWriter writer = new FileWriter(fileName)) {
    writer.write(String.format("- %-25s: %,.0f VND\n", serviceName, price));
}
```

### 7.12 Java 14+ Switch Expression

```java
// Dùng trong AppConsole, EmployeeConsole, AdminConsole...
switch (loggedUser.getRole()) {
    case ADMIN         -> AdminConsole.displayMenu(loggedUser);
    case EMPLOYEE      -> EmployeeConsole.displayMenu(loggedUser);
    case SUPPORT_STAFF -> SupportStaffConsole.displayMenu(loggedUser);
}
```

### 7.13 Text Block (Java 15+)

```java
// Dùng trong AppConsole.menuLogin()
System.out.print("""
        ============  Quản lý Đặt phòng họp & Dịch vụ Văn phòng =========
        | 1. Đăng Nhập.
        | 2. Đăng ký.
        | 0. Thoát.
        =================================================================
        """);
```

---

## 8. Design Patterns

### 8.1 Template Method Pattern ⭐ (Pattern chủ đạo)

**Vị trí:** `BaseDAO<T>`

```
BaseDAO<T>:
  executeQuery(sql, params)      ← Skeleton cố định:
    1. getConnection()             connect → prepare → setParams → execute → iterate → close
    2. prepareStatement()
    3. setParameters()           ← Bước cố định (varargs setObject)
    4. executeQuery()
    5. while(rs.next()):
         list.add(mapResultSetToObject(rs))  ← Hook: BIẾN THIÊN theo subclass
    6. close resources

BookingDAO.mapResultSetToObject()  → map tất cả Booking fields
RoomDAO.mapResultSetToObject()     → map Room fields
UserDAO.mapResultSetToObject()     → map User + Role.valueOf()
EquipmentDAO.mapResultSetToObject() → map Equipment + EquipmentStatus.valueOf()
```

**Lợi ích:** 6 DAO hoàn toàn không lặp lại code JDBC boilerplate. Thêm DAO mới chỉ cần `extends BaseDAO<T>` và implement 1 method abstract.

### 8.2 DAO Pattern

```
Presentation (BookingWizard)
    ↓ gọi
Service (BookingService.createBooking)
    ↓ gọi
DAO (BookingDAO.insertBooking)
    ↓ SQL
MySQL
```

Presentation không bao giờ biết SQL; Service không bao giờ biết câu lệnh SQL cụ thể.

### 8.3 Facade Pattern

**Vị trí:** Tất cả Service class

```
BookingWizard (Client) gọi 1 method:
    bookingService.createBooking(booking, equipList, serviceList)
        │
        └── BookingDAO.insertBooking() [Transaction]
               ├── INSERT bookings
               ├── INSERT booking_equipments (batch)
               └── INSERT booking_services (batch)
```

Presentation không biết Transaction, Batch, hay bao nhiêu bảng được insert.

### 8.4 DTO Pattern

**Vị trí:** `model/dto/BookingServiceDetail`, `BookingEquipmentDetail`

```java
// Entity sạch — ánh xạ 1:1 với bảng DB
class ServiceItem { int serviceId; String name; double price; ... }

// DTO — kết quả JOIN query, có computed field
class BookingServiceDetail {
    int serviceId;
    String serviceName;
    double price;
    int orderedQuantity;
    double getSubTotal() { return price * orderedQuantity; } // Computed
}
```

Entity không bị ô nhiễm bởi data chỉ có ý nghĩa khi JOIN.

### 8.5 Router Pattern (Thin Console Router)

```java
// AdminConsole — pure router, không chứa bất kỳ logic nào
public class AdminConsole {
    public static void displayMenu(User admin) {
        switch (choice) {
            case 1 -> UserManagement.manage(admin);     // Delegate
            case 2 -> RoomManagement.manage();           // Delegate
            case 3 -> EquipmentManagement.manage();      // Delegate
            case 4 -> BookingManagement.manage();         // Delegate
            case 5 -> ServiceManagement.manage();         // Delegate
            case 6 -> ReportManagement.manage();          // Delegate
            case 7 -> ProfileConsole.manageProfile(admin);// Delegate
        }
    }
}
```

### 8.6 Strategy Pattern (via Interface)

```java
// IBookingService là Strategy interface
// BookingService là Concrete Strategy
// BookingWizard gọi interface → có thể swap implementation không cần sửa UI
private static final IBookingService bookingService = new BookingService();
```

---

## 9. SOLID Principles

### S — Single Responsibility Principle

| Class | Trách nhiệm duy nhất |
|---|---|
| `AppConsole` | Entry point, main loop, role-based routing |
| `AuthConsole` | Login và Register flow |
| `BookingWizard` | 8-step booking wizard UI cho Employee |
| `BookingManagement` | UI duyệt/từ chối booking cho Admin |
| `UserManagement` | CRUD người dùng cho Admin |
| `TaskManagement` | Xem và cập nhật nhiệm vụ cho Support Staff |
| `BookingService` | Business rules về booking |
| `BookingDAO` | SQL operations trên bảng bookings |
| `PasswordUtil` | Hash và verify password |
| `ExportBillUtil` | Xuất hóa đơn ra file |
| `InputValidation` | Đọc và validate input từ console |
| `DatabaseConnection` | Load config và tạo JDBC connection |

**Vi phạm đã được fix:** AdminConsole (từ ~540 dòng God Object) → tách thành 6 class chuyên biệt trong `admin/`.

### O — Open/Closed Principle

```java
// BaseDAO — mở để mở rộng, đóng để sửa đổi
// Thêm NotificationDAO → chỉ extends BaseDAO<Notification>
// Không sửa BaseDAO.java

// Service Interface — mở để thêm implementation mới
// Thêm CachedBookingService implements IBookingService
// Không sửa BookingWizard
```

### L — Liskov Substitution Principle

```java
// BookingDAO extends BaseDAO<Booking>
// → Có thể dùng BookingDAO ở bất kỳ đâu cần BaseDAO<Booking>

// BookingService implements IBookingService
// → Có thể thay bằng bất kỳ impl nào thỏa contract interface
```

### I — Interface Segregation Principle

```java
// 7 interface nhỏ, tách theo domain — không có God Interface
IUserService         → User-specific (login, register, CRUD users)
IBookingService      → Booking-specific (create, approve, history, report)
IRoomService         → Room CRUD
IEquipmentService    → Equipment CRUD
IServiceItemService  → Service CRUD
ISupportStaffService → Staff task query
INotificationService → Notification CRUD + markRead

// BookingWizard chỉ import 3 interface cần thiết:
// IBookingService, IEquipmentService, IServiceItemService
// Không bị buộc phụ thuộc vào IUserService hay INotificationService
```

### D — Dependency Inversion Principle

```java
// Presentation phụ thuộc ABSTRACTION (interface), không phụ thuộc implementation
private static final IBookingService bookingService = new BookingService();
//                    ↑ Interface (abstraction)         ↑ Chỉ khởi tạo 1 lần

// BookingService phụ thuộc INotificationService, không phải NotificationDAO
private final INotificationService notificationService = new NotificationService();
```

---

## 10. Bảo mật & Xử lý ngoại lệ

### 10.1 SQL Injection Prevention

100% query dùng PreparedStatement — không có String concatenation SQL nào:

```java
// SAI (dễ bị injection)
"SELECT * FROM users WHERE username = '" + username + "'"

// ĐÚNG (trong toàn bộ project)
"SELECT * FROM users WHERE username = ?"
ps.setString(1, username); // Driver tự escape
```

Ví dụ tấn công bị chặn: Username nhập `admin' OR '1'='1` → PreparedStatement xử lý như string thường.

### 10.2 Password Security — BCrypt

| Phương pháp | Điểm yếu | Dự án |
|---|---|---|
| Plain text | Lộ ngay nếu DB bị hack | ❌ |
| MD5 / SHA-1 | Rainbow table attack | ❌ |
| BCrypt | Salt tự động + slow hash | ✅ |

```java
// Cùng password → hash khác nhau mỗi lần (do random salt)
"123456" → "$2a$12$abc..." (lần 1)
"123456" → "$2a$12$xyz..." (lần 2) ← Khác nhau!

// Nhưng checkpw vẫn hoạt động đúng
BCrypt.checkpw("123456", storedHash) → true
```

**Cost factor 12:** Mỗi hash mất ~300ms → brute-force 10M mật khẩu cần ~3.5 năm.

### 10.3 Input Validation

```java
// Email format
String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

// Số điện thoại Việt Nam
String regexPhone = "^(0|\\+84)[35789][0-9]{8}$";

// Số dương — chặn âm và 0
public static int inputPositiveInt() {
    while (true) {
        int val = inputInt();
        if (val > 0) return val;
        System.out.print("Vui lòng nhập số nguyên dương (> 0): ");
    }
}
```

### 10.4 Authorization

```java
// Role-based routing: mỗi role chỉ thấy menu của mình
switch (loggedUser.getRole()) {
    case EMPLOYEE      -> EmployeeConsole.displayMenu(loggedUser);
    case ADMIN         -> AdminConsole.displayMenu(loggedUser);
    case SUPPORT_STAFF -> SupportStaffConsole.displayMenu(loggedUser);
}

// Support Staff chỉ thấy task của mình
"WHERE supportStaffId = ?" // staffId = loggedUser.getUserId() — không thể xem task người khác

// Admin không thể tự xóa tài khoản của mình
if (userId == currentAdmin.getUserId()) {
    System.out.println("=> Không thể xóa tài khoản của chính bạn!");
    return;
}
```

### 10.5 Race Condition Double-Check

```java
// Khi Admin approve booking:
// 1. Check booking còn PENDING
// 2. Re-check phòng vẫn available (double-check concurrency)
List<Room> stillAvailable = bookingDAO.getAvailableRooms(start, end, capacity);
boolean stillFree = stillAvailable.stream().anyMatch(r -> r.getRoomId() == roomId);
if (!stillFree) throw new Exception("Xung đột lịch — phòng đã bị đặt bởi booking khác!");
```

### 10.6 Phân cấp Exception

```
AppConsole.main()
    └── [FATAL catch] → System.err.println + System.exit(1)
         ↑ Bắt mọi Exception chưa được xử lý

BookingWizard / Presentation
    └── [try-catch Exception] → In lỗi → Tiếp tục (không crash app)

BookingService.approveBooking()
    └── [throw new Exception("...")] → Caller bắt và hiển thị

BookingDAO.insertBooking()
    └── [catch SQLException] → rollback() → throw lại
```

---

## 11. Điểm nổi bật kỹ thuật

| Điểm nổi bật | Mô tả | Kỹ thuật |
|---|---|---|
| **JDBC Transaction đúng chuẩn** | Booking + Equipment + Service được insert **nguyên tử** — thành công hết hoặc rollback hết | `setAutoCommit(false)`, `commit()`, `rollback()` |
| **Batch Execution** | Insert danh sách thiết bị/dịch vụ bằng `addBatch() + executeBatch()` | JDBC Batch |
| **Overlap Detection SQL** | Phát hiện trùng lịch phòng bằng điều kiện `startTime < endTime2 AND endTime > startTime2` | SQL subquery |
| **Race Condition Double-check** | Admin approve → re-check phòng còn available trước khi UPDATE | Optimistic concurrency |
| **PreparedStatement 100%** | Không có string SQL concat nào — chống SQL Injection hoàn toàn | JDBC security |
| **BCrypt cost 12** | Mật khẩu được hash an toàn, salt tự động | jBCrypt |
| **Template Method DAO** | 6 DAO không viết lại JDBC boilerplate | Design Pattern |
| **DTO tách Entity** | `BookingServiceDetail` tách getSubTotal() ra khỏi `ServiceItem` Entity | Clean Architecture |
| **7 Service Interface** | Presentation phụ thuộc abstraction, không phụ thuộc implementation | SOLID DIP |
| **1 query cho Review** | Tính chi phí: 1 getAllServices() → Map → Stream.sum() (tránh N+1) | Performance |
| **Equipment stock validation** | Chặn mượn vượt quá `available` ngay lúc chọn | Business logic |
| **Admin User CRUD đầy đủ** | Xem/Thêm/Sửa/Xóa user với bảo vệ tự xóa | UserManagement |
| **Notification system** | Nhân viên nhận thông báo khi booking được duyệt/từ chối | NotificationDAO |
| **db.properties** | Credentials tách khỏi source code | Configuration externalization |
| **Fatal error handler** | App không crash im lặng — luôn in thông báo và exit(1) | AppConsole |

---

## 12. Phân loại kiến thức: Cơ bản → Nâng cao

### 🟢 Cơ bản (Foundation)

| Khái niệm | Ví dụ trong dự án |
|---|---|
| OOP: Class & Object | Tất cả Model classes (User, Room, Booking…) |
| OOP: Encapsulation | Private fields + getter/setter trong Model |
| OOP: Inheritance | `BookingDAO extends BaseDAO<Booking>` |
| OOP: Polymorphism | `IBookingService bookingService = new BookingService()` |
| Java Collections | `List<Room>`, `Map<Room, Integer>` |
| String formatting | `printf("%-20s", name)` trong console UI |
| try-catch-finally | Exception handling trong DAO/Service |
| Static methods | `AuthConsole.login()`, `InputValidation.inputInt()` |
| Enum | BookingStatus, Role, PreparationStatus, EquipmentStatus |
| File I/O | `FileWriter`, `File.mkdirs()` — ExportBillUtil |
| JDBC cơ bản | Connection, PreparedStatement, ResultSet |

### 🟡 Trung bình (Intermediate)

| Khái niệm | Ví dụ trong dự án |
|---|---|
| Generics | `BaseDAO<T>`, `List<T>`, `Map<K,V>` |
| Interface | 7 Service interfaces |
| Abstract class | `BaseDAO<T>` với abstract method |
| Custom Exception | `InvalidRegisterException` |
| Java 8 Lambda | `.forEach()`, `.filter()`, `.stream()` |
| Method Reference | `ServiceItem::getServiceId`, `BookingServiceDetail::getSubTotal` |
| try-with-resources | Auto-close Connection, PreparedStatement, ResultSet |
| Date-Time API | `LocalDateTime`, `DateTimeFormatter`, `Timestamp.valueOf()` |
| Regex | Email và phone validation |
| Varargs | `executeUpdate(String sql, Object... params)` |
| Properties file | `db.properties` + `getResourceAsStream()` |
| Switch Expression (14+) | `case ADMIN -> AdminConsole.displayMenu()` |
| Text Block (15+) | `menuLogin()` trong AppConsole |

### 🔴 Nâng cao (Advanced)

| Khái niệm | Ví dụ trong dự án |
|---|---|
| JDBC Transaction | `setAutoCommit(false)`, `commit()`, `rollback()` trong `insertBooking()` |
| JDBC Batch Execution | `addBatch()`, `executeBatch()` — insert nhiều rows hiệu quả |
| `getGeneratedKeys()` | Lấy auto-increment bookingId sau INSERT |
| `rs.wasNull()` | Xử lý nullable INT column (`supportStaffId`) |
| SQL Overlap Detection | `startTime < endTime2 AND endTime > startTime2` |
| Template Method Pattern | `BaseDAO<T>.executeQuery()` + abstract `mapResultSetToObject()` |
| DAO Pattern | Tách hoàn toàn Data Access Layer |
| Facade Pattern | Service layer làm Facade cho Presentation |
| DTO Pattern | `BookingServiceDetail`, `BookingEquipmentDetail` |
| Router Pattern | Thin Console Routers (AdminConsole, EmployeeConsole) |
| SOLID Principles (đầy đủ) | SRP / OCP / LSP / ISP / DIP |
| 3-Tier Architecture | Presentation → Service → DAO strict separation |
| Stream API nâng cao | `Collectors.toMap()`, `mapToDouble().sum()`, `anyMatch()` |
| Race Condition Handling | Double-check availability trước khi approve |
| BCrypt Password | Salt + slow hash + configurable work factor |
| Dependency Inversion | Presentation → Interface ← Implementation |
| Authorization by Role | Role-based menu routing + ownership check |
| Optimistic Concurrency | Double-check trước khi commit thay đổi |

---

## 13. Điểm còn hạn chế & Hướng phát triển

### 🟡 Có thể bổ sung trong console app

| Thiếu | Vì sao quan trọng | Hướng sửa |
|---|---|---|
| Unit Test | Không có test nào validate business logic | JUnit 5 + Mockito mock DAO |
| Connection Pool | Mỗi query mở/đóng 1 connection → chậm với nhiều user | HikariCP |
| Logging framework | Dùng `System.out/err.println` không có level/format | SLF4J + Logback |
| Dependency Injection Container | Service tạo DAO bằng `new` cứng | Manual DI hoặc Google Guice |
| Soft delete | `DELETE` cứng → mất data vĩnh viễn | Thêm `isDeleted` flag |
| Audit trail | Không biết ai/khi nào thay đổi trạng thái booking | Thêm `updatedAt`, `updatedBy` |
| Booking duration validation | Chưa giới hạn min/max thời gian đặt phòng | Thêm validation ở Service layer |
| Equipment available tracking | Khi booking approved, `equipment.available` không giảm | Cập nhật trong transaction |

### 🟢 Không cần thiết ở console app (không phải thiếu sót)

- REST API / WebSocket (dành cho web app)
- JWT / Authentication token (console = local process)
- Redis cache (quy mô nhỏ, không cần)
- Microservices (over-engineering)
- Input sanitization XSS (console không render HTML)

---

## 14. Tóm tắt nhanh cho báo cáo/bảo vệ

```
Tên dự án:  Hệ thống Quản lý Đặt Phòng Họp & Dịch vụ Văn phòng
Mã dự án:   PRJ-MEETING-JAVA-05
Ngôn ngữ:   Java (JDK 17+)
Database:   MySQL — JDBC (PreparedStatement, Transaction, Batch)

Kiến trúc:  3-Tier (Presentation → Service → DAO)
            6 package chính: dao, model, service, presentation, util, exception

Actor:      3 vai trò (EMPLOYEE, ADMIN, SUPPORT_STAFF)
Chức năng:  Đặt phòng wizard 8 bước, duyệt booking, quản lý CRUD toàn bộ,
            báo cáo thống kê, xuất hóa đơn .txt, thông báo real-time (pull)

Design Pattern:  Template Method (BaseDAO), DAO, Facade (Service), DTO,
                 Router (thin console), Strategy (via Interface)

SOLID:      S — 1 class 1 nhiệm vụ (12+ class chuyên biệt)
            O — mở rộng không cần sửa (BaseDAO, Service interfaces)
            L — subclass thay thế được superclass (DAO, Service impl)
            I — 7 interface nhỏ theo domain
            D — Presentation phụ thuộc Interface, không phụ thuộc Implementation

Java 8+:    Stream API, Lambda, Method Reference, Date-Time API,
            Switch Expression (14+), Text Block (15+)

Security:   BCrypt (cost 12) + PreparedStatement (anti SQL Injection)
            + Role-based authorization + Admin self-delete protection

Robustness: JDBC Transaction rollback, Fatal error handler (System.exit),
            Race condition double-check, Stack trace on fatal

Clean Code: DRY (BaseDAO), SRP (1 class 1 job), DTO tách Entity,
            db.properties (credentials externalization)
```
