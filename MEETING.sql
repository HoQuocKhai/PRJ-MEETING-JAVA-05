CREATE DATABASE PRJ_MEETING_JAVA_05;
USE PRJ_MEETING_JAVA_05;

-- 1. Bảng Người dùng
CREATE TABLE users (
                       userId INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL, -- Sẽ lưu chuỗi hash
                       roleUser ENUM ('EMPLOYEE', 'SUPPORT_STAFF', 'ADMIN') DEFAULT 'EMPLOYEE',
                       department VARCHAR(100), -- Thêm phòng ban theo SRS
                       contact VARCHAR(100),    -- Email hoặc thông tin liên hệ khác
                       phoneNumber VARCHAR(20) NOT NULL
);

-- 2. Bảng Phòng họp
CREATE TABLE rooms (
                       roomId INT PRIMARY KEY AUTO_INCREMENT,
                       roomName VARCHAR(100) NOT NULL UNIQUE,
                       capacity INT CHECK (capacity > 0) NOT NULL,
                       location VARCHAR(100),
                       fixedDevice VARCHAR(255)
);

-- 3. Bảng Thiết bị di động
CREATE TABLE equipments (
                            equipmentId INT PRIMARY KEY AUTO_INCREMENT,
                            equipmentName VARCHAR(255) NOT NULL,
                            quantity INT CHECK (quantity > 0) NOT NULL,
                            available INT CHECK (available >= 0) NOT NULL,
                            status ENUM('ACTIVE', 'MAINTENANCE', 'BROKEN') DEFAULT 'ACTIVE' -- Tình trạng hoạt động
);

-- 4. Bảng Dịch vụ đi kèm
CREATE TABLE services (
                          serviceId INT PRIMARY KEY AUTO_INCREMENT,
                          serviceName VARCHAR(255) NOT NULL,
                          unit VARCHAR(50) NOT NULL, -- Đơn vị tính (VD: Chai, Phần, Lần)
                          price DOUBLE NOT NULL      -- Đơn giá phục vụ tính năng Nâng cao số 2
);

-- ==========================================
-- PHẦN BỔ SUNG: CÁC BẢNG XỬ LÝ NGHIỆP VỤ ĐẶT PHÒNG
-- ==========================================

-- 5. Bảng Đặt phòng (Bảng trung tâm)
CREATE TABLE bookings (
                          bookingId INT PRIMARY KEY AUTO_INCREMENT,
                          userId INT NOT NULL,                  -- Nhân viên đặt phòng
                          roomId INT NOT NULL,                  -- Phòng được chọn
                          supportStaffId INT,                   -- Admin phân công sau khi duyệt
                          startTime DATETIME NOT NULL,          -- Thời gian bắt đầu
                          endTime DATETIME NOT NULL,            -- Thời gian kết thúc
                          bookingStatus ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
                          preparationStatus ENUM('PENDING', 'PREPARING', 'READY', 'MISSING_EQUIPMENT') DEFAULT 'PENDING',
                          createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (userId) REFERENCES users(userId),
                          FOREIGN KEY (roomId) REFERENCES rooms(roomId),
                          FOREIGN KEY (supportStaffId) REFERENCES users(userId)
);

-- 6. Bảng Chi tiết Thiết bị mượn thêm (Quan hệ n-n)
CREATE TABLE booking_equipments (
                                    bookingId INT NOT NULL,
                                    equipmentId INT NOT NULL,
                                    quantity INT CHECK (quantity > 0) NOT NULL,
                                    PRIMARY KEY (bookingId, equipmentId),
                                    FOREIGN KEY (bookingId) REFERENCES bookings(bookingId) ON DELETE CASCADE,
                                    FOREIGN KEY (equipmentId) REFERENCES equipments(equipmentId) ON DELETE CASCADE
);

-- 7. Bảng Chi tiết Dịch vụ đi kèm (Quan hệ n-n)
CREATE TABLE booking_services (
                                  bookingId INT NOT NULL,
                                  serviceId INT NOT NULL,
                                  quantity INT CHECK (quantity > 0) NOT NULL,
                                  PRIMARY KEY (bookingId, serviceId),
                                  FOREIGN KEY (bookingId) REFERENCES bookings(bookingId) ON DELETE CASCADE,
                                  FOREIGN KEY (serviceId) REFERENCES services(serviceId) ON DELETE CASCADE
);

-- 8. Bảng Hệ thống Thông báo (In-app Notifications)
CREATE TABLE notifications (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               userId INT NOT NULL,
                               message TEXT NOT NULL,
                               isRead BOOLEAN DEFAULT FALSE,
                               createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
);