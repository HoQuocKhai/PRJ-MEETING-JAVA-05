# PROJECT_ANALYSIS.md
# Hệ thống Quản lý Đặt Phòng Họp & Dịch vụ Văn phòng
**Mã dự án:** PRJ-MEETING-JAVA-05 | **Môn:** Java Advanced | **Cập nhật:** Sau Giai đoạn 4

---

## MỤC LỤC

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Kiến thức Java cốt lõi được sử dụng](#2-kiến-thức-java-cốt-lõi-được-sử-dụng)
3. [Design Patterns](#3-design-patterns)
4. [SOLID Principles](#4-solid-principles)
5. [Luồng hoạt động chi tiết](#5-luồng-hoạt-động-chi-tiết)
6. [Bảo mật & Chống tấn công](#6-bảo-mật--chống-tấn-công)
7. [Xử lý ngoại lệ](#7-xử-lý-ngoại-lệ)
8. [Điểm nổi bật của dự án](#8-điểm-nổi-bật-của-dự-án)
9. [Phân loại kiến thức: Cơ bản vs Nâng cao](#9-phân-loại-kiến-thức-cơ-bản-vs-nâng-cao)
10. [Điểm còn thiếu & hướng phát triển](#10-điểm-còn-thiếu--hướng-phát-triển)

---

## 1. Tổng quan kiến trúc

### 1.1 Mô hình 3-Tier (Three-Layer Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│  PRESENTATION LAYER (Console UI)                            │
│  AppConsole → AuthConsole → EmployeeConsole/AdminConsole    │
│  + employee/BookingWizard + admin/* + staff/TaskManagement  │
├─────────────────────────────────────────────────────────────┤
│  SERVICE LAYER (Business Logic)                             │
│  IXxxService interfaces → XxxService implementations        │
│  Validation + Orchestration + Transaction coordination      │
├─────────────────────────────────────────────────────────────┤
│  DATA ACCESS LAYER (DAO + JDBC)                             │
│  BaseDAO<T> (Template Method) → XxxDAO                      │
│  PreparedStatement + Connection + Transaction               │
└─────────────────────────────────────────────────────────────┘
                          ↓
                    MySQL Database
                 (PRJ_MEETING_JAVA_05)
```

**Lý do chọn 3-Tier:**
- **Separation of Concerns:** Mỗi tầng chỉ biết tầng liền kề bên dưới nó
- **Testability:** Có thể test Service độc lập mà không cần DB (mock DAO)
- **Maintainability:** Thay đổi UI không ảnh hưởng Business Logic; đổi DB không ảnh hưởng UI
- **Scalability:** Có thể thêm REST API layer mà không cần rewrite Service/DAO

### 1.2 Sơ đồ package hoàn chỉnh

```
src/
├── dao/
│   ├── BaseDAO<T>               ← Abstract class, Template Method Pattern
│   ├── UserDAO                  ← CRUD users
│   ├── RoomDAO                  ← CRUD rooms
│   ├── EquipmentDAO             ← CRUD equipment + status parsing
│   ├── ServiceItemDAO           ← CRUD services
│   ├── BookingDAO               ← Booking + Transaction + JOIN queries
│   └── NotificationDAO          ← Notification CRUD
│
├── model/
│   ├── User, Room, Equipment, ServiceItem, Booking, Notification
│   ├── BookingDetailEquipment   ← Join-table model (booking ↔ equipment)
│   ├── BookingDetailService     ← Join-table model (booking ↔ service)
│   └── dto/
│       ├── BookingServiceDetail     ← DTO: service + ordered quantity
│       └── BookingEquipmentDetail   ← DTO: equipment + borrowed quantity
│   └── Enum/
│       ├── Role                     ← EMPLOYEE, SUPPORT_STAFF, ADMIN
│       ├── BookingStatus            ← PENDING, APPROVED, REJECTED, CANCELED
│       ├── PreparationStatus        ← PENDING, PREPARING, READY, MISSING_EQUIPMENT
│       └── EquipmentStatus          ← ACTIVE, MAINTENANCE, BROKEN
│
├── service/
│   ├── IUserService, IBookingService, IRoomService
│   ├── IEquipmentService, IServiceItemService
│   ├── ISupportStaffService, INotificationService
│   └── [7 Service implementations]
│
├── presentation/
│   ├── AppConsole               ← Entry point, main router, fatal error handler
│   ├── AuthConsole              ← Login/Register
│   ├── ProfileConsole           ← Profile management
│   ├── EmployeeConsole          ← Thin router cho EMPLOYEE
│   ├── AdminConsole             ← Thin router cho ADMIN
│   ├── SupportStaffConsole      ← Thin router cho SUPPORT_STAFF
│   ├── employee/
│   │   └── BookingWizard        ← 8-step booking wizard
│   ├── admin/
│   │   ├── RoomManagement, EquipmentManagement, ServiceManagement
│   │   ├── BookingManagement, ReportManagement
│   └── staff/
│       └── TaskManagement       ← Task view & status update
│
├── util/
│   ├── DatabaseConnection       ← Load config từ db.properties
│   ├── InputValidation          ← Console input helpers + validation
│   ├── PasswordUtil             ← BCrypt hash/check
│   └── ExportBillUtil           ← Export bill to .txt file
│
└── exception/
    └── InvalidRegisterException ← Custom exception cho đăng ký
```

---

## 2. Kiến thức Java cốt lõi được sử dụng

### 2.1 Object-Oriented Programming (OOP)

#### Encapsulation (Đóng gói)
```java
// Tất cả model fields đều private, truy cập qua getter/setter
public class Booking {
    private int bookingId;
    private BookingStatus bookingStatus;
    // ...
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }
}
```
**Lý do:** Kiểm soát truy cập, dễ thay đổi implementation mà không phá vỡ caller.

#### Inheritance (Kế thừa)
```java
// Tất cả DAO đều kế thừa từ BaseDAO<T>
public class BookingDAO extends BaseDAO<Booking> {
    @Override
    protected Booking mapResultSetToObject(ResultSet rs) throws SQLException {
        // Mapping logic riêng của Booking
    }
}
```
**Lý do:** Tái sử dụng code JDBC boilerplate (`executeQuery`, `executeUpdate`...) mà không cần copy lại.

#### Polymorphism (Đa hình)
```java
// Presentation dùng interface, không biết class cụ thể
private static final IBookingService bookingService = new BookingService();
// Sau này có thể: = new CachedBookingService(); — không cần sửa code caller
```

#### Abstraction (Trừu tượng hóa)
```java
// BaseDAO định nghĩa CONTRACT: class con phải implement mapResultSetToObject()
public abstract class BaseDAO<T> {
    protected abstract T mapResultSetToObject(ResultSet rs) throws SQLException;
    // Các hàm template đã sẵn sàng dùng
}
```

### 2.2 Generics (Kiểu tổng quát)

```java
public abstract class BaseDAO<T> {
    protected List<T> executeQuery(String sql, Object... params) throws SQLException {
        List<T> list = new ArrayList<>();
        // ...
        list.add(mapResultSetToObject(rs)); // T được xác định lúc compile
        return list;
    }
}

// Sử dụng:
BookingDAO extends BaseDAO<Booking>    // T = Booking
RoomDAO    extends BaseDAO<Room>       // T = Room
```
**Lý do:** Type-safe collection mà không cần cast. Compiler phát hiện lỗi type mismatch sớm.

### 2.3 Enum (Kiểu liệt kê)

```java
public enum BookingStatus {
    PENDING, APPROVED, REJECTED, CANCELED
}

public enum PreparationStatus {
    PENDING, PREPARING, READY, MISSING_EQUIPMENT
}
```

**Lý do sử dụng Enum thay vì String:**
- **Type-safe:** Không thể gán giá trị sai (`PENDNG` sẽ là compile error)
- **Readable:** Code tường minh hơn `if (status == BookingStatus.PENDING)`
- **Switch-friendly:** Java 14+ switch expression hiệu quả
- **DB mapping:** Dùng `Enum.valueOf(string)` để map từ DB → Enum; `enum.name()` để lưu vào DB

```java
// Map từ DB (String → Enum)
b.setBookingStatus(BookingStatus.valueOf(rs.getString("bookingStatus")));

// Lưu vào DB (Enum → String)
ps.setString(6, b.getBookingStatus().name());
```

### 2.4 Collections Framework

```java
// ArrayList — ordered, mutable list
List<Room> availableRooms = new ArrayList<>();

// LinkedHashMap — ordered Map (giữ thứ tự insert, dùng cho Report)
Map<Room, Integer> stats = new LinkedHashMap<>();

// Stream API — xử lý collection theo functional style
Map<Integer, Double> priceMap = serviceItemDAO.getAllServices().stream()
        .collect(Collectors.toMap(ServiceItem::getServiceId, ServiceItem::getPrice));

// Stream filter + findFirst — tìm element theo điều kiện
Equipment selected = equipments.stream()
        .filter(e -> e.getEquipmentId() == eqId)
        .findFirst().orElse(null);

// Stream mapToDouble + sum — tính tổng
double totalCost = svList.stream()
        .mapToDouble(BookingServiceDetail::getSubTotal)
        .sum();
```

### 2.5 Lambda & Functional Interfaces (Java 8+)

```java
// Lambda thay cho anonymous class
equipments.forEach(eq ->
    System.out.printf("%-5d | %-20s\n", eq.getEquipmentId(), eq.getEquipmentName())
);

// Method reference thay cho lambda
.collect(Collectors.toMap(ServiceItem::getServiceId, ServiceItem::getPrice))

// Predicate trong filter
.filter(e -> e.getEquipmentId() == eqId)
```

### 2.6 JDBC (Java Database Connectivity)

#### PreparedStatement — phòng SQL Injection
```java
String sql = "SELECT * FROM bookings WHERE bookingId = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setInt(1, bookingId);  // ? được thay thế an toàn, không qua String concat
```

#### ResultSet — đọc kết quả từ DB
```java
while (rs.next()) {
    Booking b = new Booking();
    b.setBookingId(rs.getInt("bookingId"));
    b.setStartTime(rs.getTimestamp("startTime").toLocalDateTime());
    // ...
}
```

#### Transaction Management — đảm bảo tính nhất quán dữ liệu
```java
conn.setAutoCommit(false);   // Bắt đầu transaction
try {
    // INSERT bookings
    // INSERT booking_equipments (batch)
    // INSERT booking_services (batch)
    conn.commit();           // Tất cả thành công → commit
} catch (SQLException e) {
    conn.rollback();         // Bất kỳ lỗi nào → rollback toàn bộ
    throw e;
} finally {
    conn.setAutoCommit(true);
    conn.close();
}
```

#### Batch Execution — insert nhiều rows hiệu quả
```java
for (BookingDetailEquipment eq : eqList) {
    psEq.setInt(1, newBookingId);
    psEq.setInt(2, eq.getEquipmentId());
    psEq.setInt(3, eq.getQuantity());
    psEq.addBatch();    // Gom vào batch
}
psEq.executeBatch();    // Gửi 1 lần đến DB thay vì N lần riêng rẻ
```

#### Try-with-resources — tự động đóng resource
```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    // DB resources tự động close khi ra khỏi block, kể cả khi có exception
}
```

#### getGeneratedKeys() — lấy primary key vừa insert
```java
psBooking = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
psBooking.executeUpdate();
ResultSet rs = psBooking.getGeneratedKeys();
if (rs.next()) {
    int newBookingId = rs.getInt(1);  // Lấy ID auto_increment vừa tạo
}
```

### 2.7 Java I/O — Xuất hóa đơn file

```java
// FileWriter + try-with-resources
try (FileWriter writer = new FileWriter(fileName)) {
    writer.write(String.format("- %-20s : %,.0f VND\n", serviceName, price));
}
```

### 2.8 Date/Time API (Java 8 — java.time)

```java
// LocalDate, LocalTime, LocalDateTime — immutable, thread-safe
LocalDate date = LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
LocalTime startTime = LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm"));
LocalDateTime startDateTime = LocalDateTime.of(date, startTime);

// So sánh thời gian
if (start.isBefore(LocalDateTime.now())) { /* validation */ }
if (end.isBefore(start) || end.isEqual(start)) { /* validation */ }

// Chuyển đổi cho JDBC
Timestamp.valueOf(startDateTime)          // LocalDateTime → Timestamp (JDBC)
rs.getTimestamp("startTime").toLocalDateTime()  // Timestamp → LocalDateTime
```

### 2.9 Varargs (Object... params)

```java
// BaseDAO dùng varargs để nhận bất kỳ số tham số nào
protected boolean executeUpdate(String sql, Object... params) throws SQLException {
    // ...
    for (int i = 0; i < params.length; i++) {
        ps.setObject(i + 1, params[i]);  // setObject tự xử lý các kiểu khác nhau
    }
}

// Caller gọi linh hoạt:
executeUpdate("UPDATE ... WHERE id = ?", bookingId);
executeUpdate("INSERT ... VALUES (?, ?, ?)", id, name, value);
```

### 2.10 Password Security — BCrypt

```java
// Hash password khi đăng ký (one-way)
String hashedPass = BCrypt.hashpw(password, BCrypt.gensalt());

// Verify khi đăng nhập (so sánh hash, không so sánh plain text)
boolean isMatch = BCrypt.checkpw(inputPassword, storedHashedPassword);
```

**Lý do dùng BCrypt thay vì MD5/SHA:**
- **Salt tự động:** BCrypt tạo random salt trong mỗi lần hash → cùng password nhưng hash khác nhau
- **Slow hash:** Tốc độ chậm cố ý → chống brute-force attack
- **Work factor:** Có thể tăng cost factor khi hardware mạnh hơn

### 2.11 Properties File — Cấu hình ngoài source code

```java
// Load từ classpath (db.properties nằm trong src/)
Properties p = new Properties();
try (InputStream is = DatabaseConnection.class.getClassLoader()
        .getResourceAsStream("db.properties")) {
    p.load(is);
}
String url  = p.getProperty("db.url");
String user = p.getProperty("db.user");
String pass = p.getProperty("db.password");
```

**Lý do:** Tách credential ra ngoài source code → không commit password lên Git.

### 2.12 Custom Exception

```java
// Tạo custom exception có ý nghĩa rõ ràng hơn Exception chung
public class InvalidRegisterException extends Exception {
    public InvalidRegisterException(String message) {
        super(message);
    }
}

// Sử dụng — caller biết đây là lỗi validate đăng ký
} catch (InvalidRegisterException e) {
    System.out.println("Lỗi đăng ký: " + e.getMessage());
}
```

### 2.13 Static Factory Pattern (via static methods)

```java
// Console classes dùng static methods — không cần instantiate
AuthConsole.login();
AuthConsole.register();
BookingWizard.start(employee);

// Lý do: Console là stateless utility class, không cần nhiều instance
```

### 2.14 Null Safety

```java
// Kiểm tra wasNull() sau khi đọc nullable INT column
int supportStaffId = rs.getInt("supportStaffId");
if (!rs.wasNull()) {
    b.setSupportStaffId(supportStaffId);
} else {
    b.setSupportStaffId(null);  // Java Integer (wrapper) nhận null
}

// orElse(null) cho Optional-like behavior
Equipment selected = equipments.stream()
        .filter(e -> e.getEquipmentId() == eqId)
        .findFirst().orElse(null);  // Trả null nếu không tìm thấy
if (selected == null) { /* handle */ }
```

---

## 3. Design Patterns

### 3.1 Template Method Pattern ⭐ (Pattern chính của dự án)

**Vị trí:** `BaseDAO<T>`

**Ý tưởng:** Define skeleton của algorithm trong abstract class, để subclass fill vào các bước cụ thể.

```
BaseDAO<T>:
  executeQuery()            ← FIXED: connect → prepare → executeQuery → iterate → close
    ↓ gọi
  mapResultSetToObject()    ← VARIABLE: mỗi DAO map theo cách riêng

BookingDAO:  mapResultSetToObject() → map Booking fields
RoomDAO:     mapResultSetToObject() → map Room fields
UserDAO:     mapResultSetToObject() → map User fields
```

**Lý do chọn:**
- Loại bỏ hoàn toàn code JDBC boilerplate lặp đi lặp lại (connect, prepare, close)
- Code DRY (Don't Repeat Yourself)
- Thêm DAO mới chỉ cần extends và implement 1 hàm abstract

### 3.2 DAO Pattern (Data Access Object)

**Vị trí:** Tất cả `*DAO` class

```
Presentation                Service              DAO              DB
BookingWizard   →  bookingService.    →  bookingDAO.    →  MySQL
                   createBooking()       insertBooking()    (SQL)
```

**Lý do:**
- **Abstraction:** Service không biết SQL cụ thể, chỉ biết "lưu booking"
- **Single point of change:** Đổi DB chỉ sửa DAO, không đụng Service
- **Testable:** Có thể mock DAO để test Service logic

### 3.3 Facade Pattern

**Vị trí:** `BookingService`, `UserService`... đóng vai trò Facade

```
BookingWizard (Client)
    |
    ↓ gọi 1 method
BookingService.createBooking()  ← Facade ẩn đi sự phức tạp
    |
    ├── Validate time
    ├── bookingDAO.insertBooking() → Transaction
    │     ├── INSERT bookings
    │     ├── INSERT booking_equipments (batch)
    │     └── INSERT booking_services  (batch)
    └── (raise event/notification)
```

**Lý do:** Presentation không cần biết phức tạp bên trong (transaction, validation, multiple DAO calls).

### 3.4 DTO Pattern (Data Transfer Object)

**Vị trí:** `model/dto/BookingServiceDetail`, `model/dto/BookingEquipmentDetail`

**Vấn đề giải quyết:**
```java
// TRƯỚC — Entity bị ô nhiễm
class ServiceItem {
    int serviceId, price, ...  ← Entity fields (ánh xạ DB)
    int orderQuantity;          ← DTO field (chỉ có nghĩa khi JOIN)
}

// SAU — Entity sạch, DTO tách biệt
class ServiceItem { ... }  ← Chỉ chứa fields trong bảng services

class BookingServiceDetail {  ← DTO cho JOIN query result
    int serviceId, orderedQuantity;
    String serviceName, unit;
    double price;
    double getSubTotal() { return price * orderedQuantity; }
}
```

**Lý do:**
- Entity ánh xạ 1:1 với bảng DB
- DTO là "result type" của query phức tạp (JOIN, aggregate)
- Không vi phạm Single Responsibility của Entity

### 3.5 Router Pattern (Console Layer)

**Vị trí:** `EmployeeConsole`, `AdminConsole`, `SupportStaffConsole`

```java
// Thin router — không chứa business logic, chỉ điều hướng
public class EmployeeConsole {
    public static void displayMenu(User employee) {
        switch (choice) {
            case 1 -> BookingWizard.start(employee);         // Delegate
            case 2 -> BookingWizard.viewHistory(employee);   // Delegate
            case 3 -> ProfileConsole.manageProfile(employee);// Delegate
            case 4 -> showNotifications(employee);
        }
    }
}
```

**Lý do:** SRP — Router chỉ routing, không implement logic. Logic nằm trong specialized class.

### 3.6 Strategy Pattern (tiềm năng — chưa implement đầy đủ)

```java
// Interface IBookingService là "Strategy interface"
// BookingService là "Concrete Strategy"
// Có thể thêm: MockBookingService, CachedBookingService...
private static final IBookingService bookingService = new BookingService();
```

---

## 4. SOLID Principles

### S — Single Responsibility Principle

| Class | Trách nhiệm duy nhất |
|---|---|
| `BookingDAO` | CRUD với bảng `bookings` + related queries |
| `BookingService` | Business rules về booking (validate, orchestrate) |
| `BookingWizard` | UI wizard 8 bước để nhân viên đặt phòng |
| `BookingManagement` | UI quản lý booking cho Admin |
| `ExportBillUtil` | Xuất hóa đơn ra file |
| `PasswordUtil` | Hash/verify password |
| `InputValidation` | Đọc và validate input từ console |

**Vi phạm đã sửa:** `AdminConsole` (~540 dòng) → tách thành 5 class chuyên biệt.

### O — Open/Closed Principle

```java
// BaseDAO mở để mở rộng:
// Thêm ServiceItemDAO → chỉ cần extends BaseDAO<ServiceItem>
// Không cần sửa BaseDAO.java

// Service Interface mở để mở rộng:
// Có thể thêm CachedBookingService implements IBookingService
// Không cần sửa BookingWizard
```

### L — Liskov Substitution Principle

```java
// BookingDAO extends BaseDAO<Booking>
// BookingDAO có thể thay thế BaseDAO<Booking> ở bất kỳ đâu
// Tương tự: BookingService implements IBookingService
// → Có thể dùng bất kỳ impl nào thỏa mãn contract của interface
```

### I — Interface Segregation Principle

```java
// 7 interface nhỏ, mỗi cái cho 1 domain — không có "God Interface"
IUserService        → User-specific methods
IBookingService     → Booking-specific methods
INotificationService → Notification-specific methods
// ...

// Presentation chỉ import interface nó cần:
// BookingWizard dùng IBookingService, IEquipmentService, IServiceItemService
// Không bị phụ thuộc vào IUserService hay INotificationService
```

### D — Dependency Inversion Principle

```java
// Presentation phụ thuộc vào ABSTRACTION (interface), không phải implementation
private static final IBookingService bookingService = new BookingService();
//                    ↑ Interface                      ↑ Chỉ để khởi tạo 1 lần

// Service phụ thuộc vào interface của service khác
// BookingService → INotificationService (không phải NotificationDAO)
private final INotificationService notificationService = new NotificationService();
```

---

## 5. Luồng hoạt động chi tiết

### Luồng 1: Khởi động & Đăng nhập

```
AppConsole.main()
  ├─ [try-catch Fatal] Bao bọc toàn bộ app, print error + System.exit(1)
  ├─ DatabaseConnection.getConnection()
  │    └─ Load db.properties từ classpath → DriverManager.getConnection()
  └─ while(true) Loop chính:
       ├─ AuthConsole.login()   → UserService.login() → BCrypt.checkpw()
       ├─ AuthConsole.register() → validate → UserService.registerEmployee()
       └─ switch(user.getRole()):
            EMPLOYEE      → EmployeeConsole.displayMenu()
            ADMIN         → AdminConsole.displayMenu()
            SUPPORT_STAFF → SupportStaffConsole.displayMenu()
```

**Tính năng bảo mật tại đây:**
- Password được hash bằng BCrypt trước khi lưu
- Login dùng BCrypt.checkpw() — không bao giờ so sánh plain text

---

### Luồng 2: Nhân viên đặt phòng (BookingWizard — 8 bước)

```
BookingWizard.start(employee)
  │
  ├─ [Bước 1] inputDateTimeRange()
  │    ├─ LocalDate.parse() → DateTimeFormatter("yyyy-MM-dd")
  │    ├─ LocalTime.parse() → DateTimeFormatter("HH:mm")
  │    └─ LocalDateTime.of(date, time) → DateTimeParseException nếu sai format
  │
  ├─ [Bước 2] inputPositiveInt()  → Capacity > 0, chặn số âm
  │
  ├─ [Bước 3] bookingService.getAvailableRooms(start, end, capacity)
  │    ├─ Validate: start > now, end > start (Service layer)
  │    └─ BookingDAO.getAvailableRooms():
  │         SQL: SELECT rooms WHERE capacity >= ? AND roomId NOT IN (
  │              SELECT roomId FROM bookings
  │              WHERE status IN ('PENDING','APPROVED')
  │              AND (startTime < ? AND endTime > ?)  ← overlap detection
  │         )
  │
  ├─ [Bước 4] selectRoom() → validate roomId có trong danh sách trả về
  │
  ├─ [Bước 5] selectEquipments()
  │    ├─ equipmentService.getAllEquipments()  ← 1 lần duy nhất (I-A fix)
  │    └─ while(true):
  │         ├─ Validate ID từ danh sách thực tế (I-F fix)
  │         └─ Validate qty <= equipment.getAvailable() (I-F fix)
  │
  ├─ [Bước 6] selectServices()
  │    ├─ serviceService.getAllServices()  ← 1 lần duy nhất (I-A fix)
  │    └─ while(true):
  │         └─ Validate ID từ danh sách thực tế
  │
  ├─ [Bước 7] printReview() → calculateTotalServiceCost()
  │    └─ 1 getAllServices() query → build Map<serviceId, price> → stream sum
  │       (I-B fix: N+1 → 1 query)
  │
  └─ [Bước 8] Confirm → bookingService.createBooking()
       └─ BookingDAO.insertBooking() [TRANSACTION]:
            ├─ INSERT INTO bookings (PENDING, PENDING)
            ├─ RETURN_GENERATED_KEYS → newBookingId
            ├─ INSERT INTO booking_equipments (BATCH)
            ├─ INSERT INTO booking_services   (BATCH)
            ├─ [OK] conn.commit()
            └─ [ERROR] conn.rollback() → throw lại lỗi
```

---

### Luồng 3: Admin duyệt Booking

```
AdminConsole → BookingManagement.manage()
  ├─ bookingService.getPendingBookings()
  │    └─ BookingDAO.getPendingBookings(): SELECT * WHERE bookingStatus = 'PENDING'
  │
  ├─ Chọn bookingId + staffId để assign
  │
  └─ bookingService.approveBooking(bookingId, staffId)
       ├─ [Check 1] bookingDAO.getBookingById() → null? → throw Exception
       ├─ [Check 2] booking.getBookingStatus() == PENDING? → Nếu không → throw
       ├─ [Check 3] Double-check race condition:
       │    bookingDAO.getAvailableRooms() với thời gian booking đó
       │    → Phòng vẫn available? → Nếu không → throw "Xung đột lịch"
       ├─ bookingDAO.approveAndAssign(bookingId, staffId)
       │    SQL: UPDATE bookings SET status='APPROVED', supportStaffId=?,
       │                            preparationStatus='PREPARING'
       └─ notificationService.insertNotification(employee.userId, "Phê duyệt OK")
```

**Điểm nổi bật:** Double-check race condition là kỹ thuật quan trọng trong hệ thống concurrent — ngăn 2 admin cùng duyệt booking trùng phòng cùng lúc.

---

### Luồng 4: Support Staff cập nhật chuẩn bị

```
SupportStaffConsole → TaskManagement.manage(support)
  ├─ supportService.getTasksByStaffId(staffId)
  │    SQL: SELECT * WHERE supportStaffId=? AND bookingStatus='APPROVED'
  │         AND preparationStatus IN ('PENDING','PREPARING','MISSING_EQUIPMENT')
  │
  ├─ Validate ownership: task phải thuộc về staff đang đăng nhập
  │
  ├─ printTaskDetail(bookingId):
  │    ├─ getBookingInfo() → booking header
  │    ├─ getTaskEquipments() → BookingEquipmentDetail[] (borrowedQuantity ≠ total)
  │    └─ getTaskServices()  → BookingServiceDetail[] (orderedQuantity)
  │
  └─ updateStatus(): switch(choice):
       PREPARING        → UPDATE preparationStatus = 'PREPARING'
       READY            → UPDATE preparationStatus = 'READY'
       MISSING_EQUIPMENT → UPDATE preparationStatus = 'MISSING_EQUIPMENT'
```

---

### Luồng 5: Admin xuất hóa đơn

```
BookingManagement → bookingService.exportBill(bookingId)
  ├─ [Check] booking != null
  ├─ [Check] preparationStatus == READY (chỉ xuất khi đã chuẩn bị xong)
  ├─ getEquipmentsByBookingId() → List<BookingEquipmentDetail>
  ├─ getServicesByBookingId()   → List<BookingServiceDetail>
  ├─ totalCost = svList.stream().mapToDouble(BookingServiceDetail::getSubTotal).sum()
  └─ ExportBillUtil.exportBillToFile(booking, totalCost, eqList, svList)
       ├─ Tạo thư mục out/bills/ nếu chưa có
       ├─ Tên file: Bill_Booking_{id}_{timestamp}.txt
       └─ FileWriter → ghi header + equipment section + service section + total
```

---

### Luồng 6: Notification

```
[Khi Admin approve/reject] → BookingService:
  notificationService.insertNotification(userId, message)
  → NotificationDAO.insertNotification():
       INSERT INTO notifications (userId, message) VALUES (?, ?)

[Khi Employee đăng nhập] → EmployeeConsole:
  notificationService.getUnreadNotifications(userId)
  → SELECT * WHERE userId=? AND isRead=FALSE ORDER BY createdAt DESC
  → Hiển thị nếu có thông báo mới
  → notificationService.markAllAsRead(userId)
       UPDATE notifications SET isRead=TRUE WHERE userId=? AND isRead=FALSE
```

---

## 6. Bảo mật & Chống tấn công

### 6.1 SQL Injection Prevention ✅

**Tất cả 100% queries dùng PreparedStatement:**

```java
// SAI — dễ bị injection
String sql = "SELECT * FROM users WHERE username = '" + username + "'";

// ĐÚNG — dùng trong project
String sql = "SELECT * FROM users WHERE username = ?";
ps.setString(1, username);  // MySQL driver escape tự động
```

**Tình huống tấn công bị chặn:**
- Username nhập `admin' OR '1'='1` → PreparedStatement xử lý như string bình thường

### 6.2 Password Security ✅

| Phương pháp | Yếu điểm | Dự án dùng |
|---|---|---|
| Lưu plain text | Lộ ngay nếu DB bị hack | ❌ |
| MD5/SHA-256 hash | Rainbow table attack | ❌ |
| BCrypt với salt | Chống brute-force, rainbow table | ✅ |

```java
// BCrypt salt làm cho cùng password → khác hash mỗi lần hash
"password123" → "$2a$10$abc..." (hash lần 1)
"password123" → "$2a$10$xyz..." (hash lần 2) ← khác nhau!
// Nhưng BCrypt.checkpw("password123", stored_hash) vẫn trả về true
```

### 6.3 Input Validation ✅

```java
// Email format validation
String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

// Số điện thoại Việt Nam
String regexPhone = "^(0|\\+84)[35789][0-9]{8}$";

// Số dương — chặn số âm và 0 cho qty/capacity
public static int inputPositiveInt() {
    while (true) {
        int val = inputInt();
        if (val > 0) return val;
        System.out.print("Vui lòng nhập số nguyên dương (> 0): ");
    }
}
```

### 6.4 Authorization ✅

```java
// Mỗi role chỉ thấy màn hình của mình
switch (loggedUser.getRole()) {
    case EMPLOYEE      -> EmployeeConsole.displayMenu(loggedUser);
    case ADMIN         -> AdminConsole.displayMenu(loggedUser);
    case SUPPORT_STAFF -> SupportStaffConsole.displayMenu(loggedUser);
}

// Staff chỉ thấy task được phân công cho mình
String sql = "SELECT * FROM bookings WHERE supportStaffId = ? AND ...";
// staffId = loggedUser.getUserId() → không thể xem task người khác
```

---

## 7. Xử lý ngoại lệ

### 7.1 Hierarchy phân tầng

```
AppConsole.main()
  └─ [FATAL try-catch] Bắt mọi Exception chưa được xử lý
       → System.err.println("[FATAL]...")
       → System.exit(1)

EmployeeConsole/BookingWizard
  └─ [try-catch Exception] Bắt lỗi domain
       → System.out.println("=> Lỗi: " + e.getMessage())
       → Tiếp tục chương trình (không crash)

BookingService.approveBooking()
  └─ [throw new Exception("Booking không ở trạng thái PENDING")]
       → Caller (BookingManagement) bắt và hiển thị

BookingDAO.insertBooking()
  └─ [catch SQLException] → conn.rollback() → throw lại cho Service
       → Service catch → throw lại cho Presentation
       → Presentation catch → hiển thị cho user
```

### 7.2 Graceful degradation

```java
// AppConsole — không crash, hiển thị lỗi rồi thoát
public static void main(String[] args) {
    try {
        // Toàn bộ app logic
    } catch (Exception e) {
        System.err.println("[FATAL] Lỗi hệ thống không xử lý được: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }
}
```

### 7.3 Exception từ DB Status mapping

```java
// EquipmentDAO — không bao giờ nuốt lỗi của data không hợp lệ
try {
    return EquipmentStatus.valueOf(statusStr);
} catch (IllegalArgumentException e) {
    throw new IllegalArgumentException(
        "Trạng thái thiết bị không hợp lệ trong DB: '" + statusStr + "'. " +
        "Chỉ chấp nhận: ACTIVE, MAINTENANCE, BROKEN", e);
}
```

### 7.4 Transaction Rollback

```java
// Đảm bảo không bao giờ có dữ liệu rác trong DB
} catch (SQLException e) {
    if (conn != null) conn.rollback();  // Hủy bỏ mọi thay đổi
    throw e;  // Ném lại cho caller biết thất bại
} finally {
    conn.setAutoCommit(true);
    conn.close();  // Luôn dọn dẹp resource
}
```

---

## 8. Điểm nổi bật của dự án

### ✅ Điểm mạnh

| Điểm nổi bật | Mô tả | Kỹ thuật |
|---|---|---|
| **Transaction đúng chuẩn** | Booking + Equipment + Service được insert nguyên tử — thành công hết hoặc thất bại hết | JDBC Transaction + Rollback |
| **Double-check race condition** | Admin duyệt booking → re-check phòng vẫn available | Optimistic concurrency |
| **PreparedStatement 100%** | Không có 1 String SQL nào được concat trực tiếp | SQL Injection Prevention |
| **BCrypt password** | Không lưu plain text, salt tự động, chống brute-force | Security |
| **Template Method DAO** | 6 DAO không viết lại code JDBC boilerplate | DRY principle |
| **DTO tách biệt** | Entity không bị ô nhiễm bởi JOIN data | Clean Architecture |
| **Interface cho DIP** | 7 Service interface — Presentation phụ thuộc abstraction | SOLID-D |
| **1-query cho Review** | calculateTotalServiceCost: 1 query thay vì N queries | Performance |
| **Validate equipment stock** | Chặn mượn quá số lượng khả dụng | Business logic |
| **Overlap detection** | SQL NOT IN + thời gian overlap logic đúng chuẩn | Booking logic |

### ✅ Kiến trúc rõ ràng sau refactor

- **AdminConsole:** 540 dòng God Object → 5 specialized class
- **EmployeeConsole:** Monolithic → thin router + BookingWizard
- **NotificationService:** Presentation không gọi NotificationDAO trực tiếp
- **db.properties:** Credentials ra ngoài source code

---

## 9. Phân loại kiến thức: Cơ bản vs Nâng cao

### 🟢 Kiến thức Cơ bản (Foundation)

| Khái niệm | Ví dụ trong dự án |
|---|---|
| OOP: Class & Object | Tất cả Model classes (User, Room, Booking...) |
| OOP: Encapsulation | Private fields + getter/setter trong Model |
| OOP: Inheritance | `BookingDAO extends BaseDAO<Booking>` |
| Java Collections | `List<Room>`, `Map<Room, Integer>` |
| String formatting | `String.format("%-20s", name)` trong console UI |
| try-catch-finally | Exception handling trong DAO/Service |
| Static methods | `AuthConsole.login()`, `InputValidation.inputInt()` |
| Enum | BookingStatus, Role, PreparationStatus |
| File I/O | `FileWriter`, `File.mkdirs()` — ExportBillUtil |
| JDBC cơ bản | Connection, Statement, ResultSet |

### 🟡 Kiến thức Trung bình (Intermediate)

| Khái niệm | Ví dụ trong dự án |
|---|---|
| Generics | `BaseDAO<T>`, `List<T>`, `Map<K,V>` |
| Interface | 7 Service interfaces (IBookingService...) |
| PreparedStatement | Tất cả DAO queries |
| Abstract class | BaseDAO với abstract method |
| Custom Exception | `InvalidRegisterException` |
| Java 8 Lambda | `.forEach()`, `.filter()`, `.stream()` |
| try-with-resources | Auto-close Connection, PreparedStatement, ResultSet |
| Date-Time API | `LocalDateTime`, `DateTimeFormatter`, `Timestamp` |
| Regex | Email/phone validation trong InputValidation |
| Varargs | `executeUpdate(String sql, Object... params)` |
| Properties | `db.properties` + `getResourceAsStream()` |

### 🔴 Kiến thức Nâng cao (Advanced)

| Khái niệm | Ví dụ trong dự án |
|---|---|
| JDBC Transaction | `setAutoCommit(false)`, `commit()`, `rollback()` |
| Batch Execution | `addBatch()`, `executeBatch()` trong insertBooking |
| Template Method Pattern | `BaseDAO<T>.executeQuery()` + abstract `mapResultSetToObject()` |
| DAO Pattern | Tách hoàn toàn Data Access Layer |
| Facade Pattern | Service layer làm Facade |
| DTO Pattern | `BookingServiceDetail`, `BookingEquipmentDetail` |
| SOLID Principles | DIP with service interfaces, SRP per class |
| 3-Tier Architecture | Presentation → Service → DAO strict separation |
| Stream API | `Collectors.toMap()`, `mapToDouble().sum()`, method reference |
| Race Condition handling | Double-check availability trước khi approve |
| BCrypt Password | Salt + slow hash + work factor |
| SQL Overlap Detection | `startTime < end AND endTime > start` logic |
| Dependency Inversion | Presentation → Interface ← Implementation |
| `getGeneratedKeys()` | Lấy auto-increment ID sau INSERT |

---

## 10. Điểm còn thiếu & hướng phát triển

### 🟡 Thiếu trong phạm vi console app (có thể bổ sung)

| Thiếu | Lý do quan trọng | Hướng sửa |
|---|---|---|
| Unit Test | Không có test nào validate business logic | JUnit 5 + Mockito mock DAO |
| Dependency Injection Container | Service tạo DAO bằng `new` cứng | Google Guice hoặc manual DI |
| Connection Pool | Mỗi query mở/đóng connection → chậm | HikariCP hoặc c3p0 |
| Logging framework | Dùng `System.out.println` và `System.err.println` | SLF4J + Logback |
| Cấu hình môi trường | `db.properties` đơn giản, không phân biệt dev/prod | Multi-profile config |
| Input sanitization | Chỉ validate format, chưa sanitize XSS cho nội dung | Strip HTML/script tags |

### 🔴 Thiếu về thiết kế dữ liệu

| Thiếu | Mô tả |
|---|---|
| Audit trail | Không lưu who/when thay đổi trạng thái booking |
| Soft delete | `DELETE` cứng — mất data vĩnh viễn, không có `isDeleted` flag |
| Booking duration validation | Không kiểm tra tối thiểu/tối đa thời gian phòng (vd: 30 phút đến 8 tiếng) |
| Equipment availability update | Khi booking approved, không cần cập nhật `equipment.available` (vì app không track real-time) |

### 🟢 Điểm không cần thiết ở console app

- REST API, WebSocket (dành cho web app)
- Authentication token/JWT (app console = local)
- Redis cache (console app nhỏ, không cần)
- Microservices (over-engineering cho quy mô này)

---

## Tóm tắt kỹ thuật cho báo cáo/bảo vệ

```
Kiến trúc:   3-Tier (Presentation → Service → DAO)
Database:    MySQL + JDBC (PreparedStatement, Transaction, Batch)
Pattern:     Template Method, DAO, Facade, DTO, Router
OOP:         Encapsulation, Inheritance, Polymorphism, Abstraction
SOLID:       Áp dụng đầy đủ S, O, L, I, D với interface + thin routers
Java 8+:     Stream API, Lambda, Method Reference, Date-Time API
Security:    BCrypt password, PreparedStatement (anti SQL Injection)
Robustness:  Transaction rollback, Fatal error handler, Race condition check
Clean Code:  DRY (BaseDAO), SRP (1 class 1 nhiệm vụ), DTO tách Entity
```
