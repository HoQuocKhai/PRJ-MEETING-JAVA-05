package dao;

import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {

    // 1. Hàm trừu tượng: Bắt buộc các class con (RoomDAO, EquipmentDAO)
    // phải tự định nghĩa cách map dữ liệu từ DB sang Object của riêng chúng.
    protected abstract T mapResultSetToObject(ResultSet rs) throws SQLException;

    // 2. Hàm dùng chung cho các câu lệnh SELECT trả về một Danh sách (List)
    protected List<T> executeQuery(String sql, Object... params) throws SQLException {
        List<T> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params); // Gọi hàm set tham số

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToObject(rs)); // Trả quyền map cho class con
                }
            }
        }
        return list;
    }

    // 3. Hàm dùng chung cho các câu lệnh SELECT trả về 1 đối tượng duy nhất (getById)
    protected T executeQueryForSingleObject(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToObject(rs);
                }
            }
        }
        return null;
    }

    // 4. Hàm dùng chung cho INSERT / UPDATE / DELETE
    protected boolean executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);
            return ps.executeUpdate() > 0;
        }
    }

    // Hàm phụ trợ tự động set tham số vào dấu ?
    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }
}