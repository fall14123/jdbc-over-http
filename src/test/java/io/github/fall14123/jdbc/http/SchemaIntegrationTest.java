package io.github.fall14123.jdbc.http;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.sql.*;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Generic integration tests that run against all schemas.
 * Requires: Flock on localhost:8080, DuckDB httpserver on localhost:9999
 */
public class SchemaIntegrationTest {

    record SchemaConfig(String name, String url, String user, String password) {}

    static Stream<SchemaConfig> schemas() {
        return Stream.of(
            new SchemaConfig("flock", "jdbc:http://localhost:8080/v1/query", null, null),
            new SchemaConfig("httpserver", "jdbc:http://localhost:9999/", "user", "pass")
        );
    }

    private Connection getConnection(SchemaConfig config) throws SQLException {
        Properties props = new Properties();
        props.setProperty("schema", config.name());
        if (config.user() != null) props.setProperty("user", config.user());
        if (config.password() != null) props.setProperty("password", config.password());
        return DriverManager.getConnection(config.url(), props);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("schemas")
    void testSimpleQuery(SchemaConfig config) throws SQLException {
        try (Connection conn = getConnection(config);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 as num, 'hello' as greeting")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("num"));
            assertEquals("hello", rs.getString("greeting"));
            assertFalse(rs.next());
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("schemas")
    void testVersion(SchemaConfig config) throws SQLException {
        try (Connection conn = getConnection(config);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version()")) {
            assertTrue(rs.next());
            String version = rs.getString(1);
            assertNotNull(version);
            assertTrue(version.startsWith("v"));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("schemas")
    void testMultipleRows(SchemaConfig config) throws SQLException {
        try (Connection conn = getConnection(config);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM (VALUES (1, 'a'), (2, 'b'), (3, 'c')) AS t(id, name)")) {
            int count = 0;
            while (rs.next()) {
                count++;
                assertNotNull(rs.getInt(1));
                assertNotNull(rs.getString(2));
            }
            assertEquals(3, count);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("schemas")
    void testMetadata(SchemaConfig config) throws SQLException {
        try (Connection conn = getConnection(config);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 42 as answer, 'test' as value")) {
            ResultSetMetaData meta = rs.getMetaData();
            assertEquals(2, meta.getColumnCount());
            assertEquals("answer", meta.getColumnName(1));
            assertEquals("value", meta.getColumnName(2));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("schemas")
    void testPreparedStatement(SchemaConfig config) throws SQLException {
        try (Connection conn = getConnection(config);
             PreparedStatement stmt = conn.prepareStatement("SELECT ? as val, ? as name")) {
            stmt.setInt(1, 42);
            stmt.setString(2, "test");
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(42, rs.getInt("val"));
                assertEquals("test", rs.getString("name"));
            }
        }
    }
}
