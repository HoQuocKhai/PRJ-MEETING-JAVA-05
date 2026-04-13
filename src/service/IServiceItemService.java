package service;

import model.ServiceItem;
import java.util.List;

/**
 * Interface cho ServiceItemService.
 * Áp dụng Dependency Inversion Principle (DIP).
 */
public interface IServiceItemService {
    List<ServiceItem> getAllServices() throws Exception;
    boolean addService(String name, String unit, double price, String description) throws Exception;
    boolean updateService(int id, String name, String unit, double price, String description) throws Exception;
    boolean deleteService(int id) throws Exception;
}
