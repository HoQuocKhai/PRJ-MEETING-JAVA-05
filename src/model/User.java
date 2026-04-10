package model;

import model.Enum.Role;

public class User {
    private int userId;
    private String username;
    private String password;
    private String department;
    private String phoneNumber;
    private String contact;
    private Role role;

    public User() {}

    public User(int userId, String username, String password, String department, String phoneNumber, String contact, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.contact = contact;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
