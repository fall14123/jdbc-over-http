package io.github.fall14123.jdbc.http;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests specific to flock schema.
 * Requires: Flock service running on localhost:8080
 */
public class FlockSchemaTest {

    private Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("schema", "flock");
        return DriverManager.getConnection("jdbc:http://localhost:8080/v1/query", props);
    }

    // Add flock-specific tests here if needed
}
