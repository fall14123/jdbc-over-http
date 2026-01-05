package com.fall14123.jdbc.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;
import javax.net.ssl.HttpsURLConnection;

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
        if (!acceptsURL(url)) {
            return null;
        }

        try {
            URL serverUrl = parseJdbcUrl(url);
            String username = info.getProperty("user", "");
            String password = info.getProperty("password", "");
            
            return new HttpJdbcConnection(serverUrl, username, password, objectMapper);
        } catch (Exception e) {
            throw new SQLException("Failed to connect to HTTP JDBC server", e);
        }
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null && (url.startsWith(URL_PREFIX) || url.startsWith(URL_PREFIX_SSL));
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    private URL parseJdbcUrl(String url) throws MalformedURLException {
        String httpUrl;
        if (url.startsWith(URL_PREFIX)) {
            httpUrl = "http://" + url.substring(URL_PREFIX.length());
        } else if (url.startsWith(URL_PREFIX_SSL)) {
            httpUrl = "https://" + url.substring(URL_PREFIX_SSL.length());
        } else {
            throw new MalformedURLException("Invalid JDBC URL: " + url);
        }
        
        return new URL(httpUrl);
    }
}