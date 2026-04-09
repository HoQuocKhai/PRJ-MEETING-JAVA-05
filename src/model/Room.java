package model;

public class Room {
    private int roomId;
    private String roomName;
    private int capacity;
    private String location;
    private String fixedDevice;

    public Room() {}

    public Room(int roomId, String roomName, int capacity, String location, String fixedDevice) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
        this.location = location;
        this.fixedDevice = fixedDevice;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFixedDevice() {
        return fixedDevice;
    }

    public void setFixedDevice(String fixedDevice) {
        this.fixedDevice = fixedDevice;
    }
}
