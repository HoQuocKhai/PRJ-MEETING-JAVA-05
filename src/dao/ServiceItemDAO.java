package dao;

import model.ServiceItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ServiceItemDAO extends BaseDAO<ServiceItem> {

    @Override
    protected ServiceItem mapResultSetToObject(ResultSet rs) throws SQLException {
        ServiceItem s = new ServiceItem();
        s.setServiceId(rs.getInt("serviceId"));
        s.setServiceName(rs.getString("serviceName"));
        s.setUnit(rs.getString("unit"));
        s.setPrice(rs.getDouble("price"));
        s.setDescription(rs.getString("description"));
        return s;
    }

    public List<ServiceItem> getAllServices() throws SQLException {
        return executeQuery("SELECT * FROM services");
    }

    public ServiceItem getServiceById(int serviceId) throws SQLException {
        return executeQueryForSingleObject("SELECT * FROM services WHERE serviceId=?", serviceId);
    }

    public boolean insertService(ServiceItem service) throws SQLException {
        String sql = "INSERT INTO services (serviceName, unit, price, description) VALUES (?, ?, ?, ?)";
        return executeUpdate(sql, service.getServiceName(), service.getUnit(), service.getPrice(), service.getDescription());
    }

    public boolean updateService(ServiceItem service) throws SQLException {
        String sql = "UPDATE services SET serviceName=?, unit=?, price=?, description=? WHERE serviceId=?";
        return executeUpdate(sql, service.getServiceName(), service.getUnit(), service.getPrice(), service.getDescription(), service.getServiceId());
    }

    public boolean deleteService(int serviceId) throws SQLException {
        return executeUpdate("DELETE FROM services WHERE serviceId=?", serviceId);
    }
}
