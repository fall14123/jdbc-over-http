package com.fall14123.jdbc.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.sql.*;
import java.util.*;

public class HttpJdbcDriver implements Driver {
    private static final String URL_PREFIX = "jdbc:http://";
    private static final String URL_PREFIX_SSL = "jdbc:https://";
    private final ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            DriverManager.registerDriver(new HttpJdbcDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register HttpJdbcDriver", e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) return null;

        try {
            URL serverUrl = parseJdbcUrl(url);
            String username = info.getProperty("user", "");
            String password = info.getProperty("password", "");
            LogLevel logLevel = LogLevel.fromString(info.getProperty("logLevel", System.getProperty("jdbc.http.log.level", "INFO")));
            int connectTimeout = parseInt(info.getProperty("connectTimeout", System.getProperty("jdbc.http.connect.timeout", "30000")), 30000);
            int readTimeout = parseInt(info.getProperty("readTimeout", System.getProperty("jdbc.http.read.timeout", "60000")), 60000);
            boolean keepAlive = Boolean.parseBoolean(info.getProperty("keepAlive", System.getProperty("jdbc.http.keep.alive", "true")));
            
            // Load schema config - default to "flock", can be overridden via property
            String schemaName = info.getProperty("schema", System.getProperty("jdbc.http.schema", "flock"));
            SchemaConfig schema = SchemaConfig.load(schemaName);
            
            return new HttpJdbcConnection(serverUrl, username, password, objectMapper, logLevel, connectTimeout, readTimeout, keepAlive, schema);
        } catch (Exception e) {
            throw new SQLException("Failed to connect to HTTP JDBC server", e);
        }
    }

    @Override
    public boolean acceptsURL(String url) {
        return url != null && (url.startsWith(URL_PREFIX) || url.startsWith(URL_PREFIX_SSL));
    }

    @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
    @Override public int getMajorVersion() { return 1; }
    @Override public int getMinorVersion() { return 0; }
    @Override public boolean jdbcCompliant() { return false; }
    @Override public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }

    private URL parseJdbcUrl(String url) throws MalformedURLException {
        if (url.startsWith(URL_PREFIX)) return new URL("http://" + url.substring(URL_PREFIX.length()));
        if (url.startsWith(URL_PREFIX_SSL)) return new URL("https://" + url.substring(URL_PREFIX_SSL.length()));
        throw new MalformedURLException("Invalid JDBC URL: " + url);
    }

    private int parseInt(String value, int defaultValue) {
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return defaultValue; }
    }
}
