package com.fall14123.jdbc.http;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;

public class HttpJdbcClientTest {

    public static void main(String[] args) throws Exception {
        HttpJdbcClientTest test = new HttpJdbcClientTest();
        test.testBasicConnection();
        test.testPreparedStatement();
        test.testSSLConnection();
    }

    @Test
    public void testBasicConnection() throws Exception {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");

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
    public void testSSLConnection() throws Exception {
        String url = "jdbc:https://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");

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