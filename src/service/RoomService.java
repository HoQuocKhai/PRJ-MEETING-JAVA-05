package service;

import dao.RoomDAO;
import model.Room;
import java.util.List;

public class RoomService implements IRoomService {
    private final RoomDAO roomDAO = new RoomDAO();

    public List<Room> getAllRooms() throws Exception {
        return roomDAO.getAllRooms();
    }

    public List<Room> getRoomsByName(String keyword) throws Exception {
        return roomDAO.getRoomByName(keyword);
    }

    public boolean addRoom(String name, int capacity, String location, String devices) throws Exception {
        Room room = new Room(0, name, capacity, location, devices);
        return roomDAO.insertRoom(room);
    }

    public boolean updateRoom(int roomId, String name, int capacity, String location, String devices) throws Exception {
        Room existingRoom = roomDAO.getRoomById(roomId);
        if (existingRoom == null) {
            throw new Exception("Không tìm thấy phòng họp có ID: " + roomId);
        }
        
        existingRoom.setRoomName(name);
        existingRoom.setCapacity(capacity);
        existingRoom.setLocation(location);
        existingRoom.setFixedDevice(devices);
        
        return roomDAO.updateRoom(existingRoom);
    }

    public boolean deleteRoom(int roomId) throws Exception {
        Room existingRoom = roomDAO.getRoomById(roomId);
        if (existingRoom == null) {
            throw new Exception("Không tìm thấy phòng họp có ID: " + roomId);
        }
        return roomDAO.deleteRoom(roomId);
    }
}