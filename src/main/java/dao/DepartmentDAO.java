package dao;

import database.DBConnection;
import model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> findAll() throws SQLException {
        String sql = """
            SELECT d.id, d.name, COUNT(s.id) AS student_count
              FROM departments d
              LEFT JOIN students s ON s.department_id = d.id
             GROUP BY d.id, d.name
             ORDER BY d.name
            """;
        List<Department> result = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Department dept = new Department(rs.getInt("id"), rs.getString("name"));
                dept.setStudentCount(rs.getInt("student_count"));
                result.add(dept);
            }
        }
        return result;
    }

    public int countAll() throws SQLException {
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM departments")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public void insert(String name) throws SQLException {
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO departments (name) VALUES (?)")) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    public void update(int id, String name) throws SQLException {
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE departments SET name = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM departments WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
