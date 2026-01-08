package com.fall14123.jdbc.http;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for flock schema.
 * Requires: Flock service running on localhost:8080
 */
public class FlockSchemaTest {

    private Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("schema", "flock");
        return DriverManager.getConnection("jdbc:http://localhost:8080/v1/query", props);
    }

    @Test
    void testSimpleQuery() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 as num, 'hello' as greeting")) {
            
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("num"));
            assertEquals("hello", rs.getString("greeting"));
            assertFalse(rs.next());
        }
    }

    @Test
    void testVersion() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version()")) {
            
            assertTrue(rs.next());
            String version = rs.getString(1);
            assertNotNull(version);
            assertTrue(version.startsWith("v"));
        }
    }

    @Test
    void testMultipleRows() throws SQLException {
        try (Connection conn = getConnection();
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

    @Test
    void testPreparedStatement() throws SQLException {
        try (Connection conn = getConnection();
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

    @Test
    void testMetadata() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 42 as answer, 'test' as value")) {
            
            ResultSetMetaData meta = rs.getMetaData();
            assertEquals(2, meta.getColumnCount());
            assertEquals("answer", meta.getColumnName(1));
            assertEquals("value", meta.getColumnName(2));
        }
    }
}
