package service;

import model.Room;
import java.util.List;

/**
 * Interface cho RoomService.
 * Áp dụng Dependency Inversion Principle (DIP).
 */
public interface IRoomService {
    List<Room> getAllRooms() throws Exception;
    List<Room> getRoomsByName(String keyword) throws Exception;
    boolean addRoom(String name, int capacity, String location, String devices) throws Exception;
    boolean updateRoom(int roomId, String name, int capacity, String location, String devices) throws Exception;
    boolean deleteRoom(int roomId) throws Exception;
}
