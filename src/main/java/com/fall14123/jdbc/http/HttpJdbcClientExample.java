package com.fall14123.jdbc.http;

import java.sql.*;
import java.util.Properties;

public class HttpJdbcClientExample {
    
    public static void main(String[] args) {
        String url = "jdbc:http://localhost:9999/";
        
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        
        try {
            System.out.println("Testing HTTP JDBC Driver...");
            
            try (Connection conn = DriverManager.getConnection(url, props)) {
                System.out.println("✓ Connection established successfully");
                
                System.out.println("\nTesting basic query...");
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT 'hello' as greeting, version() as version");
                    
                    System.out.println("Query results:");
                    while (rs.next()) {
                        System.out.println("  Greeting: " + rs.getString("greeting"));
                        System.out.println("  Version: " + rs.getString("version"));
                    }
                }
                
                System.out.println("\nTesting prepared statement...");
                String sql = "SELECT ? as message, ? as number, ? as flag";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, "Hello World");
                    pstmt.setInt(2, 42);
                    pstmt.setBoolean(3, true);
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        System.out.println("Prepared statement results:");
                        while (rs.next()) {
                            System.out.println("  Message: " + rs.getString("message"));
                            System.out.println("  Number: " + rs.getInt("number"));
                            System.out.println("  Flag: " + rs.getBoolean("flag"));
                        }
                    }
                }
                
                System.out.println("\nTesting database metadata...");
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("  Database: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
                System.out.println("  Driver: " + meta.getDriverName() + " " + meta.getDriverVersion());
                System.out.println("  JDBC: " + meta.getJDBCMajorVersion() + "." + meta.getJDBCMinorVersion());
                System.out.println("  URL: " + meta.getURL());
            }
            
            System.out.println("\n✓ All tests completed successfully!");
            
        } catch (SQLException e) {
            System.err.println("✗ SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}