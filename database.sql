-- =========================================================================
-- DATABASE BAN ĐẦU CHO HỆ THỐNG QUẢN LÝ PHÒNG HỌP VÀ DỊCH VỤ VĂN PHÒNG
-- Tên dự án: PRJ-MEETING-JAVA-05
-- =========================================================================

-- Tùy chọn: Xóa database cũ nếu đã tồn tại và tạo lại (Bỏ comment nếu cần, lưu ý xoá DB cũ!)
-- DROP DATABASE IF EXISTS meeting_manager;
-- CREATE DATABASE meeting_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE meeting_manager;

-- ==================== TẠO BẢNG ====================

CREATE TABLE users (
    userId INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    roleUser ENUM('EMPLOYEE','SUPPORT_STAFF','ADMIN') NOT NULL,
    contact VARCHAR(100),
    phoneNumber VARCHAR(15),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rooms (
    roomId INT AUTO_INCREMENT PRIMARY KEY,
    roomName VARCHAR(100) UNIQUE NOT NULL,
    capacity INT NOT NULL,
    location VARCHAR(150),
    fixedDevice TEXT
);

CREATE TABLE equipments (
    equipmentId INT AUTO_INCREMENT PRIMARY KEY,
    equipmentName VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    available INT NOT NULL DEFAULT 0,
    status ENUM('ACTIVE','MAINTENANCE','BROKEN') DEFAULT 'ACTIVE'
);

CREATE TABLE services (
    serviceId INT AUTO_INCREMENT PRIMARY KEY,
    serviceName VARCHAR(100) NOT NULL,
    unit VARCHAR(30),
    price DOUBLE NOT NULL DEFAULT 0,
    description TEXT
);

CREATE TABLE bookings (
    bookingId INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    roomId INT NOT NULL,
    supportStaffId INT NULL,
    startTime DATETIME NOT NULL,
    endTime DATETIME NOT NULL,
    participants INT DEFAULT 1,
    bookingStatus ENUM('PENDING','APPROVED','REJECTED','CANCELED') DEFAULT 'PENDING',
    preparationStatus ENUM('PENDING','PREPARING','READY','MISSING_EQUIPMENT') DEFAULT 'PENDING',
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId),
    FOREIGN KEY (roomId) REFERENCES rooms(roomId),
    FOREIGN KEY (supportStaffId) REFERENCES users(userId)
);

CREATE TABLE booking_equipments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bookingId INT NOT NULL,
    equipmentId INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (bookingId) REFERENCES bookings(bookingId) ON DELETE CASCADE,
    FOREIGN KEY (equipmentId) REFERENCES equipments(equipmentId)
);

CREATE TABLE booking_services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bookingId INT NOT NULL,
    serviceId INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (bookingId) REFERENCES bookings(bookingId) ON DELETE CASCADE,
    FOREIGN KEY (serviceId) REFERENCES services(serviceId)
);

CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    message TEXT NOT NULL,
    isRead BOOLEAN DEFAULT FALSE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
);

-- ==================== DỮ LIỆU MẪU (SEED DATA) ====================
-- Lưu ý: Password các tài khoản này đều là BCrypt hash của chuỗi: "123456"
-- Mật khẩu thật tương ứng: 123456
-- Hash hợp lệ 60 ký tự, cost=12, tương thích với jbcrypt BCrypt.hashpw()

INSERT INTO users (username, password, department, roleUser, contact, phoneNumber) VALUES 
('admin1',       '$2a$12$2dD/NhxBl4O3dGmEd5BVguaOiTVn7iUVMhVGPuGAkINPT2XhTLmPa', 'Hành Chính Nhân Sự', 'ADMIN',         'admin1@company.com',   '0912345678'),
('support_linh', '$2a$12$2dD/NhxBl4O3dGmEd5BVguaOiTVn7iUVMhVGPuGAkINPT2XhTLmPa', 'Quản Trị Tòa Nhà',  'SUPPORT_STAFF', 'support1@company.com', '0987654321'),
('employee_dat', '$2a$12$2dD/NhxBl4O3dGmEd5BVguaOiTVn7iUVMhVGPuGAkINPT2XhTLmPa', 'Phát Triển Phần Mềm','EMPLOYEE',      'employ1@company.com',  '0345678901'),
('employee_mai', '$2a$12$2dD/NhxBl4O3dGmEd5BVguaOiTVn7iUVMhVGPuGAkINPT2XhTLmPa', 'Marketing',         'EMPLOYEE',      'employ2@company.com',  '0356789012');

INSERT INTO rooms (roomName, capacity, location, fixedDevice) VALUES 
('Phòng Hội Đồng', 20, 'Tầng 3 - Tòa A', 'Ghế da, Màn hình LED 85inch, Loa âm trần, Micro không dây, Máy lạnh độc lập'),
('Phòng Đào Tạo', 30, 'Tầng 2 - Tòa B', 'Bàn liền ghế, Máy chiếu Panasonic, Bảng kính, Loa treo tường'),
('Phòng Phỏng Vấn (Nhỏ)', 5, 'Tầng 1 - Tòa A', 'Bàn tròn nhỏ, Màn hình TV 40inch, Quạt máy lạnh');

INSERT INTO equipments (equipmentName, quantity, available, status) VALUES 
('Máy chiếu cầm tay Sony', 5, 5, 'ACTIVE'),
('Micro không dây Sony', 10, 8, 'ACTIVE'),
('Bảng mica Flipchart', 3, 3, 'ACTIVE'),
('Loa kéo di động JBL', 2, 1, 'ACTIVE'),
('Webcam góc rộng Logitech', 4, 0, 'MAINTENANCE');

INSERT INTO services (serviceName, unit, price, description) VALUES 
('Nước suối Dasani 500ml', 'Chai', 5000, 'Nước suối đóng chai dùng cho các cuộc họp ngắn'),
('Trà & Coffee Tươi', 'Người', 25000, 'Dịch vụ trà Lipton, Coffee hòa tan tự phục vụ (bao gồm ly sứ)'),
('Bánh ngọt (Tea Break standard)', 'Set/Người', 50000, 'Bồm 3 loại bánh ngọt, trái cây và trà nóng theo khẩu phần'),
('Dọn dẹp tăng cường sau cuộc họp', 'Lần', 100000, 'Yêu cầu dọn dẹp vệ sinh phòng họp sau khi dùng bữa hoặc meeting dài');

-- ==================== CHÚC MỪNG BẠN ĐÃ IMPORT KHỞI TẠO THÀNH CÔNG! ====================
