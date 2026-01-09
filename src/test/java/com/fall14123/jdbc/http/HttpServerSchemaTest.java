package com.fall14123.jdbc.http;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;

/**
 * Integration tests specific to httpserver schema (DuckDB httpserver extension).
 * Requires: DuckDB with httpserver extension running on localhost:9999
 */
public class HttpServerSchemaTest {

    private Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("schema", "httpserver");
        return DriverManager.getConnection("jdbc:http://localhost:9999/", props);
    }

    @Test
    void testDateParameters() throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT ? as d1, ? as d2")) {
            pstmt.setDate(1, Date.valueOf("2023-12-25"));
            pstmt.setDate(2, Date.valueOf("2024-01-01"));
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                assertEquals("2023-12-25", rs.getDate("d1").toString());
                assertEquals("2024-01-01", rs.getDate("d2").toString());
            }
        }
    }

    @Test
    void testTimeParameters() throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT ? as t1, ? as t2")) {
            pstmt.setTime(1, Time.valueOf("14:30:45"));
            pstmt.setTime(2, Time.valueOf("09:15:30"));
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                assertEquals("14:30:45", rs.getTime("t1").toString());
                assertEquals("09:15:30", rs.getTime("t2").toString());
            }
        }
    }

    @Test
    void testTimestampParameters() throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT ? as ts1, ? as ts2")) {
            pstmt.setTimestamp(1, Timestamp.valueOf("2023-12-25 14:30:45.123"));
            pstmt.setTimestamp(2, Timestamp.valueOf("2024-01-01 09:15:30.456"));
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                assertNotNull(rs.getTimestamp("ts1"));
                assertNotNull(rs.getTimestamp("ts2"));
            }
        }
    }

    @Test
    void testTableOperations() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS test_events (id INTEGER, name VARCHAR(100), event_date DATE)");
            stmt.executeUpdate("INSERT INTO test_events VALUES (1, 'Event1', '2024-01-01')");
            stmt.executeUpdate("INSERT INTO test_events VALUES (2, 'Event2', '2023-12-25')");

            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM test_events WHERE event_date >= ?")) {
                pstmt.setDate(1, Date.valueOf("2024-01-01"));
                try (ResultSet rs = pstmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt("id"));
                    assertFalse(rs.next());
                }
            }
            stmt.executeUpdate("DROP TABLE test_events");
        }
    }

    @Test
    void testSSLConnection() {
        Properties props = new Properties();
        props.setProperty("schema", "httpserver");
        try (Connection conn = DriverManager.getConnection("jdbc:https://localhost:9999/", props);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 'secure' as connection")) {
            assertTrue(rs.next());
        } catch (SQLException e) {
            // Expected if SSL not configured
            assertTrue(e.getMessage().contains("SSL") || e.getMessage().contains("PKIX") || e.getMessage().contains("Connection"));
        }
    }

    private static void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }

    private static void assertFalse(boolean condition) {
        org.junit.jupiter.api.Assertions.assertFalse(condition);
    }

    private static void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }

    private static void assertNotNull(Object obj) {
        org.junit.jupiter.api.Assertions.assertNotNull(obj);
    }
}
