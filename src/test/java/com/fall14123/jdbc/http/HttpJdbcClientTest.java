package com.fall14123.jdbc.http;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class HttpJdbcClientTest {

    public static void main(String[] args) throws Exception {
        HttpJdbcClientTest test = new HttpJdbcClientTest();
        test.testBasicConnection();
        test.testPreparedStatement();
        test.testDateParameters();
        test.testTimeParameters();
        test.testTimestampParameters();
        test.testDateFilter();
        test.testTimeFilter();
        test.testTimestampFilter();
        test.testDateTableFilter();
        test.testTimeTableFilter();
        test.testTimestampTableFilter();
        test.testSSLConnection();
    }

    @Test
    public void testBasicConnection() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            System.out.println("Connection successful!");
            
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 'hello' as greeting, version() as version");
                
                while (rs.next()) {
                    System.out.println("Greeting: " + rs.getString("greeting"));
                    System.out.println("Version: " + rs.getString("version"));
                }
            }
        }
    }

    @Test
    public void testPreparedStatement() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            String sql = "SELECT ? as message, ? as number";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "Hello World");
                pstmt.setInt(2, 42);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("Message: " + rs.getString("message"));
                        System.out.println("Number: " + rs.getString("number"));
                    }
                }
            }
        }
    }

    @Test
    public void testDateParameters() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            String sql = "SELECT ? as input_date, ? as another_date";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Date testDate = Date.valueOf("2023-12-25");
                Date anotherDate = Date.valueOf("2024-01-01");
                pstmt.setDate(1, testDate);
                pstmt.setDate(2, anotherDate);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("Input Date: " + rs.getDate("input_date"));
                        System.out.println("Another Date: " + rs.getDate("another_date"));
                    }
                }
            }
        }
    }

    @Test
    public void testTimeParameters() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            String sql = "SELECT ? as input_time, ? as another_time";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Time testTime = Time.valueOf("14:30:45");
                Time anotherTime = Time.valueOf("09:15:30");
                pstmt.setTime(1, testTime);
                pstmt.setTime(2, anotherTime);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("Input Time: " + rs.getTime("input_time"));
                        System.out.println("Another Time: " + rs.getTime("another_time"));
                    }
                }
            }
        }
    }

    @Test
    public void testTimestampParameters() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            String sql = "SELECT ? as input_timestamp, ? as another_timestamp";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Timestamp testTimestamp = Timestamp.valueOf("2023-12-25 14:30:45.123");
                Timestamp anotherTimestamp = Timestamp.valueOf("2024-01-01 09:15:30.456");
                pstmt.setTimestamp(1, testTimestamp);
                pstmt.setTimestamp(2, anotherTimestamp);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("Input Timestamp: " + rs.getTimestamp("input_timestamp"));
                        System.out.println("Another Timestamp: " + rs.getTimestamp("another_timestamp"));
                    }
                }
            }
        }
    }

    @Test
    public void testDateFilter() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            String sql = "SELECT 'Event A' as event_name, ? as event_date WHERE ? >= ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Date eventDate = Date.valueOf("2023-12-25");
                Date filterDate = Date.valueOf("2023-12-20");
                
                pstmt.setDate(1, eventDate);
                pstmt.setDate(2, eventDate);
                pstmt.setDate(3, filterDate);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("Filtered Event: " + rs.getString("event_name"));
                        System.out.println("Event Date: " + rs.getDate("event_date"));
                    }
                }
            }
        }
    }

    @Test
    public void testTimeFilter() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            String sql = "SELECT 'Morning Meeting' as event_name, ? as event_time WHERE ? > ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Time eventTime = Time.valueOf("10:30:00");
                Time filterTime = Time.valueOf("09:00:00");
                
                pstmt.setTime(1, eventTime);
                pstmt.setTime(2, eventTime);
                pstmt.setTime(3, filterTime);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("Filtered Event: " + rs.getString("event_name"));
                        System.out.println("Event Time: " + rs.getTime("event_time"));
                    }
                }
            }
        }
    }

    @Test
    public void testTimestampFilter() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            String sql = "SELECT 'Data Update' as event_name, ? as event_timestamp WHERE ? BETWEEN ? AND ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Timestamp eventTimestamp = Timestamp.valueOf("2023-12-25 14:30:45.123");
                Timestamp startFilter = Timestamp.valueOf("2023-12-25 10:00:00.000");
                Timestamp endFilter = Timestamp.valueOf("2023-12-25 18:00:00.000");
                
                pstmt.setTimestamp(1, eventTimestamp);
                pstmt.setTimestamp(2, eventTimestamp);
                pstmt.setTimestamp(3, startFilter);
                pstmt.setTimestamp(4, endFilter);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("Filtered Event: " + rs.getString("event_name"));
                        System.out.println("Event Timestamp: " + rs.getTimestamp("event_timestamp"));
                    }
                }
            }
        }
    }

    @Test
    public void testDateTableFilter() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            // Create table
            try (Statement stmt = conn.createStatement()) {
                // System.out.println("DEBUG: About to execute CREATE TABLE");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS events (id INTEGER, name VARCHAR(100), event_date DATE)");
                // System.out.println("DEBUG: CREATE TABLE completed");
                
                // Insert dummy data
                // System.out.println("DEBUG: About to execute INSERT statements");
                stmt.executeUpdate("INSERT INTO events VALUES (1, 'New Year', '2024-01-01')");
                // System.out.println("DEBUG: INSERT 1 completed");
                stmt.executeUpdate("INSERT INTO events VALUES (2, 'Christmas', '2023-12-25')");
                // System.out.println("DEBUG: INSERT 2 completed");
                stmt.executeUpdate("INSERT INTO events VALUES (3, 'Independence Day', '2024-07-04')");
                // System.out.println("DEBUG: INSERT 3 completed");
                stmt.executeUpdate("INSERT INTO events VALUES (4, 'Halloween', '2023-10-31')");
                // System.out.println("DEBUG: INSERT 4 completed");
            }
            
            // Filter by date
            String sql = "SELECT * FROM events WHERE event_date >= ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Date filterDate = Date.valueOf("2024-01-01");
                pstmt.setDate(1, filterDate);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("Events from 2024 onwards:");
                    while (rs.next()) {
                        System.out.println("ID: " + rs.getInt("id") + 
                                         ", Name: " + rs.getString("name") + 
                                         ", Date: " + rs.getDate("event_date"));
                    }
                }
            }
            
            // Cleanup
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE events");
            }
        }
    }

    @Test
    public void testTimeTableFilter() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            // Create table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS meetings (id INTEGER, title VARCHAR(100), meeting_time TIME)");
                
                // Insert dummy data
                stmt.executeUpdate("INSERT INTO meetings VALUES (1, 'Morning Standup', '09:00:00')");
                stmt.executeUpdate("INSERT INTO meetings VALUES (2, 'Lunch Break', '12:30:00')");
                stmt.executeUpdate("INSERT INTO meetings VALUES (3, 'Afternoon Review', '15:00:00')");
                stmt.executeUpdate("INSERT INTO meetings VALUES (4, 'Evening Planning', '18:00:00')");
            }
            
            // Filter by time
            String sql = "SELECT * FROM meetings WHERE meeting_time BETWEEN ? AND ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Time startTime = Time.valueOf("10:00:00");
                Time endTime = Time.valueOf("16:00:00");
                pstmt.setTime(1, startTime);
                pstmt.setTime(2, endTime);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("Meetings between 10:00 and 16:00:");
                    while (rs.next()) {
                        System.out.println("ID: " + rs.getInt("id") + 
                                         ", Title: " + rs.getString("title") + 
                                         ", Time: " + rs.getTime("meeting_time"));
                    }
                }
            }
            
            // Cleanup
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE meetings");
            }
        }
    }

    @Test
    public void testTimestampTableFilter() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            // Create table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS logs (id INTEGER, message VARCHAR(100), created_at TIMESTAMP)");
                
                // Insert dummy data
                stmt.executeUpdate("INSERT INTO logs VALUES (1, 'System started', '2024-01-01 08:00:00.000')");
                stmt.executeUpdate("INSERT INTO logs VALUES (2, 'User login', '2024-01-01 09:15:30.500')");
                stmt.executeUpdate("INSERT INTO logs VALUES (3, 'Data processed', '2024-01-01 14:30:45.123')");
                stmt.executeUpdate("INSERT INTO logs VALUES (4, 'System shutdown', '2024-01-01 18:45:00.999')");
            }
            
            // Filter by timestamp range
            String sql = "SELECT * FROM logs WHERE created_at >= ? AND created_at <= ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Timestamp startTime = Timestamp.valueOf("2024-01-01 09:00:00.000");
                Timestamp endTime = Timestamp.valueOf("2024-01-01 15:00:00.000");
                pstmt.setTimestamp(1, startTime);
                pstmt.setTimestamp(2, endTime);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("Log entries between 09:00 and 15:00:");
                    while (rs.next()) {
                        System.out.println("ID: " + rs.getInt("id") + 
                                         ", Message: " + rs.getString("message") + 
                                         ", Timestamp: " + rs.getTimestamp("created_at"));
                    }
                }
            }
            
            // Cleanup
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE logs");
            }
        }
    }

    @Test
    public void testSSLConnection() throws Exception {
        String url = "jdbc:https://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            System.out.println("SSL Connection successful!");
            
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 'secure' as connection");
                
                while (rs.next()) {
                    System.out.println("Connection: " + rs.getString("connection"));
                }
            }
        } catch (SQLException e) {
            System.out.println("SSL connection failed (expected if SSL not configured): " + e.getMessage());
        }
    }
}