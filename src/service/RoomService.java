package service;

import dao.RoomDAO;
import model.Room;
import java.util.List;

public class RoomService {
    private final RoomDAO roomDAO = new RoomDAO();

    public void displayAllRooms() {
        try {
            List<Room> rooms = roomDAO.getAllRooms();
            if (rooms.isEmpty()) {
                System.out.println("Hiện tại chưa có phòng họp nào trong hệ thống.");
                return;
            }
            System.out.println("\n--- DANH SÁCH PHÒNG HỌP ---");
            System.out.printf("%-5s | %-20s | %-10s | %-20s | %-30s\n", "ID", "Tên Phòng", "Sức chứa", "Vị trí", "Thiết bị cố định");
            System.out.println("---------------------------------------------------------------------------------------------");
            for (Room r : rooms) {
                System.out.printf("%-5d | %-20s | %-10d | %-20s | %-30s\n",
                        r.getRoomId(), r.getRoomName(), r.getCapacity(), r.getLocation(), r.getFixedDevice());
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy danh sách phòng: " + e.getMessage());
        }
    }

    public void displayRoomsByName(String keyword) {
        try {
            List<Room> rooms = roomDAO.getRoomByName(keyword);
            if (rooms.isEmpty()) {
                System.out.println("Hiện tại không có phòng nào tên có tên trên.");
                return;
            }
            System.out.println("\n--- DANH SÁCH PHÒNG HỌP THEO TÊN " + keyword + " ---");
            System.out.printf("%-5s | %-20s | %-10s | %-20s | %-30s\n", "ID", "Tên Phòng", "Sức chứa", "Vị trí", "Thiết bị cố định");
            System.out.println("---------------------------------------------------------------------------------------------");
            for (Room r : rooms) {
                System.out.printf("%-5d | %-20s | %-10d | %-20s | %-30s\n",
                        r.getRoomId(), r.getRoomName(), r.getCapacity(), r.getLocation(), r.getFixedDevice());
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy danh sách phòng: " + e.getMessage());
        }
    }

    public void addRoom(String name, int capacity, String location, String devices) {
        try {
            // TODO: Bạn có thể gọi roomDAO.isRoomNameExist(name) ở đây để chặn trùng tên
            Room room = new Room(0, name, capacity, location, devices);
            if (roomDAO.insertRoom(room)) {
                System.out.println("=> Thêm phòng họp thành công!");
            }
        } catch (Exception e) {
            System.out.println("=> Thêm phòng thất bại (Có thể do trùng tên): " + e.getMessage());
        }
    }

    public void updateRoom(int roomId, String name, int capacity, String location, String devices) {
        try {
            Room existingRoom = roomDAO.getRoomById(roomId);
            if (existingRoom == null) {
                System.out.println("=> Không tìm thấy phòng họp có ID: " + roomId);
                return;
            }
            
            existingRoom.setRoomName(name);
            existingRoom.setCapacity(capacity);
            existingRoom.setLocation(location);
            existingRoom.setFixedDevice(devices);
            
            if (roomDAO.updateRoom(existingRoom)) {
                System.out.println("=> Cập nhật thông tin phòng họp thành công!");
            } else {
                System.out.println("=> Cập nhật thông tin phòng họp thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi cập nhật phòng họp: " + e.getMessage());
        }
    }

    public void deleteRoom(int roomId) {
        try {
            Room existingRoom = roomDAO.getRoomById(roomId);
            if (existingRoom == null) {
                System.out.println("=> Không tìm thấy phòng họp có ID: " + roomId);
                return;
            }
            
            if (roomDAO.deleteRoom(roomId)) {
                System.out.println("=> Xóa phòng họp thành công!");
            } else {
                System.out.println("=> Xóa phòng họp thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi xóa phòng họp (có thể do phòng đang được đặt): " + e.getMessage());
        }
    }
}