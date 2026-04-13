package service;

import dao.ServiceItemDAO;
import model.ServiceItem;
import java.util.List;

public class ServiceItemService implements IServiceItemService {
    private final ServiceItemDAO serviceItemDAO = new ServiceItemDAO();

    public List<ServiceItem> getAllServices() throws Exception {
        return serviceItemDAO.getAllServices();
    }

    public boolean addService(String name, String unit, double price, String description) throws Exception {
        ServiceItem s = new ServiceItem(0, name, unit, price, description);
        return serviceItemDAO.insertService(s);
    }

    public boolean updateService(int id, String name, String unit, double price, String description) throws Exception {
        ServiceItem existing = serviceItemDAO.getServiceById(id);
        if (existing == null) {
            throw new Exception("Không tìm thấy dịch vụ có ID: " + id);
        }
        existing.setServiceName(name);
        existing.setUnit(unit);
        existing.setPrice(price);
        existing.setDescription(description);
        
        return serviceItemDAO.updateService(existing);
    }

    public boolean deleteService(int id) throws Exception {
        ServiceItem existing = serviceItemDAO.getServiceById(id);
        if (existing == null) {
            throw new Exception("Không tìm thấy dịch vụ có ID: " + id);
        }
        return serviceItemDAO.deleteService(id);
    }
}
