package service;

import model.User;
import java.util.List;

/**
 * Interface cho UserService.
 * Áp dụng Dependency Inversion Principle (DIP):
 * Presentation phụ thuộc vào abstraction này, không phụ thuộc vào class cụ thể.
 */
public interface IUserService {
    boolean checkUsername(String username) throws Exception;
    void registerEmployee(String username, String password, String department, String contact, String phone) throws Exception;
    void createStaffAdmin(String username, String password, model.Enum.Role role, String department, String contact, String phone) throws Exception;
    User login(String username, String password) throws Exception;
    boolean updateProfile(User user) throws Exception;
    List<User> getSupportStaffs() throws Exception;
}
