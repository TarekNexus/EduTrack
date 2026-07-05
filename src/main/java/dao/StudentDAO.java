package dao;

import database.DBConnection;
import model.DashboardStats;
import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private static final String SELECT_BASE = """
        SELECT s.*, d.name AS department_name
          FROM students s
          LEFT JOIN departments d ON d.id = s.department_id
        """;

    public List<Student> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY s.id DESC";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapAll(rs);
        }
    }

    public List<Student> search(String keyword) throws SQLException {
        String sql = SELECT_BASE + """
             WHERE s.student_code ILIKE ? OR s.full_name ILIKE ? OR d.name ILIKE ?
             ORDER BY s.id DESC
            """;
        String like = "%" + keyword + "%";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                return mapAll(rs);
            }
        }
    }

    public Student findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE s.id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public int insert(Student s) throws SQLException {
        String sql = """
            INSERT INTO students
                (student_code, full_name, email, phone, address, gender,
                 department_id, semester, cgpa, blood_group, photo_path)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, s);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void update(Student s) throws SQLException {
        String sql = """
            UPDATE students
               SET student_code = ?, full_name = ?, email = ?, phone = ?, address = ?,
                   gender = ?, department_id = ?, semester = ?, cgpa = ?, blood_group = ?, photo_path = ?
             WHERE id = ?
            """;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, s);
            ps.setInt(12, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("""
                    SELECT COUNT(*) AS total,
                           COUNT(*) FILTER (WHERE gender = 'Male')   AS male_count,
                           COUNT(*) FILTER (WHERE gender = 'Female') AS female_count
                      FROM students
                    """)) {
                if (rs.next()) {
                    stats.setTotalStudents(rs.getInt("total"));
                    stats.setMaleCount(rs.getInt("male_count"));
                    stats.setFemaleCount(rs.getInt("female_count"));
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM departments")) {
                rs.next();
                stats.setTotalDepartments(rs.getInt(1));
            }
        }

        String recentSql = SELECT_BASE + " ORDER BY s.created_at DESC LIMIT 5";
        try (PreparedStatement ps = conn.prepareStatement(recentSql);
             ResultSet rs = ps.executeQuery()) {
            stats.setRecentStudents(mapAll(rs));
        }
        return stats;
    }

    private void bind(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getStudentCode());
        ps.setString(2, s.getFullName());
        ps.setString(3, s.getEmail());
        ps.setString(4, s.getPhone());
        ps.setString(5, s.getAddress());
        ps.setString(6, s.getGender());
        if (s.getDepartmentId() != null) {
            ps.setInt(7, s.getDepartmentId());
        } else {
            ps.setNull(7, Types.INTEGER);
        }
        ps.setString(8, s.getSemester());
        ps.setBigDecimal(9, s.getCgpa());
        ps.setString(10, s.getBloodGroup());
        ps.setString(11, s.getPhotoPath());
    }

    private List<Student> mapAll(ResultSet rs) throws SQLException {
        List<Student> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapRow(rs));
        }
        return result;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setStudentCode(rs.getString("student_code"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        s.setGender(rs.getString("gender"));
        int deptId = rs.getInt("department_id");
        s.setDepartmentId(rs.wasNull() ? null : deptId);
        s.setDepartmentName(rs.getString("department_name"));
        s.setSemester(rs.getString("semester"));
        s.setCgpa(rs.getBigDecimal("cgpa"));
        s.setBloodGroup(rs.getString("blood_group"));
        s.setPhotoPath(rs.getString("photo_path"));
        var createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            s.setCreatedAt(createdAt.toLocalDateTime());
        }
        return s;
    }
}
