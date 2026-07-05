package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Resolves the Neon/PostgreSQL connection settings and hands out a JDBC
 * connection. A single connection is kept open and reused for the lifetime
 * of the app (this is a single-user desktop app, not a concurrent server),
 * since opening a fresh TLS connection to Neon on every click was the main
 * cause of the UI feeling slow. If the connection goes stale (e.g. Neon's
 * serverless compute suspended after being idle), it's transparently
 * reconnected on the next call.
 *
 * Connection settings are read (first match wins) from:
 *   1. Environment variables: DB_URL, DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, DB_SSLMODE
 *   2. ./config.properties next to the jar / working directory
 *   3. config.properties on the classpath (src/main/resources)
 *
 * A full connection string (db.url / DB_URL) - in "postgresql://" or
 * "jdbc:postgresql://" form - takes priority over individual host/port keys.
 */
public final class DBConnection {

    private static final Properties PROPS = new Properties();
    private static Connection sharedConnection;

    static {
        loadClasspathFile();
        loadWorkingDirectoryFile();
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC driver not found on classpath.", e);
        }
    }

    private DBConnection() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (sharedConnection == null || sharedConnection.isClosed() || !sharedConnection.isValid(2)) {
            sharedConnection = DriverManager.getConnection(resolveJdbcUrl(), resolveUser(), resolvePassword());
        }
        return sharedConnection;
    }

    /** Verifies connectivity and creates the schema if it doesn't exist yet. */
    public static void initSchema() throws SQLException {
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS departments (
                    id   SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL UNIQUE
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id              SERIAL PRIMARY KEY,
                    student_code    VARCHAR(30) NOT NULL UNIQUE,
                    full_name       VARCHAR(150) NOT NULL,
                    email           VARCHAR(150) NOT NULL UNIQUE,
                    phone           VARCHAR(30),
                    address         VARCHAR(255),
                    gender          VARCHAR(10),
                    department_id   INTEGER REFERENCES departments(id) ON DELETE SET NULL,
                    semester        VARCHAR(10),
                    cgpa            NUMERIC(3,2),
                    blood_group     VARCHAR(5),
                    photo_path      VARCHAR(255),
                    created_at      TIMESTAMP NOT NULL DEFAULT now()
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS admins (
                    id            SERIAL PRIMARY KEY,
                    username      VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(100) NOT NULL
                )
                """);

            if (departmentCountIsZero(stmt)) {
                stmt.execute("""
                    INSERT INTO departments (name) VALUES
                    ('CSE'), ('EEE'), ('Civil'), ('Textile')
                    """);
            }
        }
    }

    private static boolean departmentCountIsZero(Statement stmt) throws SQLException {
        try (var rs = stmt.executeQuery("SELECT COUNT(*) FROM departments")) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }

    private static void loadClasspathFile() {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException ignored) {
            // classpath config is optional
        }
    }

    private static void loadWorkingDirectoryFile() {
        Path path = Path.of("config.properties");
        if (Files.exists(path)) {
            try (InputStream in = new FileInputStream(path.toFile())) {
                PROPS.load(in);
            } catch (IOException ignored) {
                // fall through to environment variables
            }
        }
    }

    private static String get(String propertyKey, String envKey) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromProps = PROPS.getProperty(propertyKey);
        return fromProps == null ? null : fromProps.trim();
    }

    private static String resolveJdbcUrl() {
        String rawUrl = get("db.url", "DB_URL");
        if (rawUrl != null && !rawUrl.isBlank()) {
            return toJdbcUrl(rawUrl);
        }

        String host = get("db.host", "DB_HOST");
        String port = valueOrDefault(get("db.port", "DB_PORT"), "5432");
        String name = get("db.name", "DB_NAME");
        String sslmode = valueOrDefault(get("db.sslmode", "DB_SSLMODE"), "require");

        if (host == null || name == null) {
            throw new IllegalStateException(
                "No database configuration found. Create config.properties from " +
                "config.properties.example (or set DB_URL) with your Neon connection details.");
        }
        return "jdbc:postgresql://" + host + ":" + port + "/" + name + "?sslmode=" + sslmode;
    }

    private static String resolveUser() {
        String url = get("db.url", "DB_URL");
        if (url != null) {
            String[] creds = extractCredentialsFromUrl(url);
            if (creds[0] != null) return creds[0];
        }
        String user = get("db.user", "DB_USER");
        if (user == null) {
            throw new IllegalStateException("Database user not configured (db.user / DB_USER).");
        }
        return user;
    }

    private static String resolvePassword() {
        String url = get("db.url", "DB_URL");
        if (url != null) {
            String[] creds = extractCredentialsFromUrl(url);
            if (creds[1] != null) return creds[1];
        }
        String password = get("db.password", "DB_PASSWORD");
        if (password == null) {
            throw new IllegalStateException("Database password not configured (db.password / DB_PASSWORD).");
        }
        return password;
    }

    private static String toJdbcUrl(String rawUrl) {
        String url = rawUrl.trim();
        if (url.startsWith("jdbc:")) {
            return url;
        }
        if (url.startsWith("postgresql://") || url.startsWith("postgres://")) {
            String withoutScheme = url.substring(url.indexOf("://") + 3);
            String afterAt = withoutScheme.contains("@")
                ? withoutScheme.substring(withoutScheme.indexOf('@') + 1)
                : withoutScheme;
            return "jdbc:postgresql://" + stripUnsupportedParams(afterAt);
        }
        throw new IllegalStateException("Unrecognized database URL format: " + url);
    }

    /**
     * Neon's connection strings (written for libpq/psql) include params like
     * "channel_binding" that the PostgreSQL JDBC driver doesn't recognize and
     * will reject at connect time. Strip anything JDBC doesn't understand.
     */
    private static String stripUnsupportedParams(String hostAndQuery) {
        int queryStart = hostAndQuery.indexOf('?');
        if (queryStart < 0) {
            return hostAndQuery;
        }
        String base = hostAndQuery.substring(0, queryStart);
        String query = hostAndQuery.substring(queryStart + 1);
        StringBuilder kept = new StringBuilder();
        for (String param : query.split("&")) {
            if (param.isBlank() || param.startsWith("channel_binding=")) {
                continue;
            }
            if (!kept.isEmpty()) kept.append('&');
            kept.append(param);
        }
        return kept.isEmpty() ? base : base + "?" + kept;
    }

    private static String[] extractCredentialsFromUrl(String rawUrl) {
        String url = rawUrl.trim();
        String withoutScheme = url.contains("://") ? url.substring(url.indexOf("://") + 3) : url;
        if (!withoutScheme.contains("@")) {
            return new String[] { null, null };
        }
        String userInfo = withoutScheme.substring(0, withoutScheme.indexOf('@'));
        if (!userInfo.contains(":")) {
            return new String[] { userInfo, null };
        }
        String[] parts = userInfo.split(":", 2);
        return new String[] { parts[0], parts[1] };
    }

    private static String valueOrDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
