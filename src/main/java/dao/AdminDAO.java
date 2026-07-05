package dao;

import database.DBConnection;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminDAO {

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    /** Creates the first admin account (admin / admin123) if none exists yet. */
    public void ensureSeedAdmin() throws SQLException {
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admins")) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO admins (username, password_hash) VALUES (?, ?)")) {
                ps.setString(1, DEFAULT_USERNAME);
                ps.setString(2, hash(DEFAULT_PASSWORD));
                ps.executeUpdate();
            }
        }
    }

    public boolean verifyLogin(String username, String plainPassword) throws SQLException {
        String sql = "SELECT password_hash FROM admins WHERE username = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                return rs.getString("password_hash").equals(hash(plainPassword));
            }
        }
    }

    /**
     * Simplified password hashing (SHA-256 with a static pepper) suitable for
     * a school project. A production system should use a salted, adaptive
     * hash such as bcrypt or Argon2 instead.
     */
    private String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((plainPassword + ":studenthub-pepper").getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
