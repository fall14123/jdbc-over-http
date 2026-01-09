package io.github.fall14123.jdbc.http;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class JdbcConnectionManager {
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    
    private String jdbcUrl = System.getProperty("jdbc.url", "jdbc:postgresql://localhost:5432/test");
    private String driverClass = System.getProperty("jdbc.driver");

    public JdbcConnectionManager() {
        if (driverClass != null) {
            try {
                Class.forName(driverClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("JDBC driver not found: " + driverClass, e);
            }
        }
    }

    public Connection getConnection(String username, String password) throws SQLException {
        String key = username + ":" + password;
        
        return connections.computeIfAbsent(key, k -> {
            try {
                Properties props = new Properties();
                if (username != null && !username.isEmpty()) {
                    props.setProperty("user", username);
                }
                if (password != null && !password.isEmpty()) {
                    props.setProperty("password", password);
                }
                
                Connection conn = DriverManager.getConnection(jdbcUrl, props);
                conn.setAutoCommit(true);
                
                return conn;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create database connection", e);
            }
        });
    }

    public void closeAllConnections() {
        connections.values().forEach(conn -> {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        });
        connections.clear();
    }
}