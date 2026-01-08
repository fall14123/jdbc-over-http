package com.fall14123.jdbc.http;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for httpserver schema (DuckDB httpserver extension).
 * Requires: DuckDB with httpserver extension running on localhost:9999
 */
public class HttpServerSchemaTest {

    private Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("schema", "httpserver");
        return DriverManager.getConnection("jdbc:http://localhost:9999/", props);
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
