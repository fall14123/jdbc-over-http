package io.github.fall14123.jdbc.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

public class HttpJdbcConnection implements Connection {
    private final URL serverUrl;
    private final String username;
    private final String password;
    private final ObjectMapper objectMapper;
    private final HttpJdbcLogger logger;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final boolean keepAlive;
    private final SchemaConfig schema;
    private boolean closed = false;
    private boolean autoCommit = true;

    public HttpJdbcConnection(URL serverUrl, String username, String password, ObjectMapper objectMapper, 
                             LogLevel logLevel, int connectTimeoutMs, int readTimeoutMs, boolean keepAlive, 
                             SchemaConfig schema) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.objectMapper = objectMapper;
        this.logger = new HttpJdbcLogger("HttpJdbcConnection", logLevel);
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.keepAlive = keepAlive;
        this.schema = schema;
    }

    public SchemaConfig getSchemaConfig() { return schema; }

    @Override
    public Statement createStatement() throws SQLException {
        checkClosed();
        return new HttpJdbcStatement(this, logger.getLogLevel());
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkClosed();
        return new HttpJdbcPreparedStatement(this, sql, logger.getLogLevel());
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatement not supported");
    }

    @Override public String nativeSQL(String sql) { return sql; }
    @Override public void setAutoCommit(boolean autoCommit) throws SQLException { checkClosed(); this.autoCommit = autoCommit; }
    @Override public boolean getAutoCommit() throws SQLException { checkClosed(); return autoCommit; }
    @Override public void commit() throws SQLException { checkClosed(); if (autoCommit) throw new SQLException("Cannot commit when auto-commit is enabled"); }
    @Override public void rollback() throws SQLException { checkClosed(); if (autoCommit) throw new SQLException("Cannot rollback when auto-commit is enabled"); }
    @Override public void close() { closed = true; }
    @Override public boolean isClosed() { return closed; }
    @Override public DatabaseMetaData getMetaData() throws SQLException { checkClosed(); return new HttpJdbcDatabaseMetaData(this); }
    @Override public void setReadOnly(boolean readOnly) throws SQLException { checkClosed(); }
    @Override public boolean isReadOnly() throws SQLException { checkClosed(); return false; }
    @Override public void setCatalog(String catalog) throws SQLException { checkClosed(); }
    @Override public String getCatalog() throws SQLException { checkClosed(); return null; }
    @Override public void setTransactionIsolation(int level) throws SQLException { checkClosed(); }
    @Override public int getTransactionIsolation() throws SQLException { checkClosed(); return Connection.TRANSACTION_NONE; }
    @Override public SQLWarning getWarnings() throws SQLException { checkClosed(); return null; }
    @Override public void clearWarnings() throws SQLException { checkClosed(); }

    private void checkClosed() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
    }

    public QueryResult executeQuery(String sql) throws SQLException {
        return executeQuery(sql, List.of());
    }

    public QueryResult executeQuery(String sql, List<QueryRequest.Parameter> parameters) throws SQLException {
        try {
            URL requestUrl = schema.urlSuffix.isEmpty() ? serverUrl : new URL(serverUrl.toString() + schema.urlSuffix);
            HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", schema.requestContentType);
            conn.setDoOutput(true);
            conn.setConnectTimeout(connectTimeoutMs);
            conn.setReadTimeout(readTimeoutMs);
            conn.setRequestProperty("Connection", keepAlive ? "keep-alive" : "close");

            if (username != null && !username.isEmpty()) {
                conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
            }

            String body = buildRequestBody(sql, parameters);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                return schema.responseNdjson ? parseNdjsonResponse(conn.getInputStream()) : parseJsonResponse(conn.getInputStream());
            } else {
                String errorBody = readFully(conn.getErrorStream());
                try {
                    String error = JsonPath.read(errorBody, schema.errorPath);
                    throw new SQLException(error);
                } catch (PathNotFoundException e) {
                    throw new SQLException("Server error: " + errorBody);
                }
            }
        } catch (IOException e) {
            throw new SQLException("Failed to execute query over HTTP", e);
        }
    }

    private String buildRequestBody(String sql, List<QueryRequest.Parameter> parameters) throws IOException {
        // For plain text requests or empty parameter template, inline parameters into SQL
        if (schema.requestContentType.equals("text/plain") || schema.parameterTemplate.isEmpty()) {
            return inlineParameters(sql, parameters);
        }
        String paramsJson = "[]";
        if (!parameters.isEmpty()) {
            List<String> paramStrings = new ArrayList<>();
            for (QueryRequest.Parameter p : parameters) {
                String valueJson = p == null || p.value() == null ? "null" : objectMapper.writeValueAsString(p.value());
                String typeStr = p == null ? "null" : p.type();
                paramStrings.add(schema.parameterTemplate.replace("${value}", valueJson).replace("${type}", typeStr));
            }
            paramsJson = "[" + String.join(",", paramStrings) + "]";
        }
        return schema.requestTemplate.replace("${sql}", sql).replace("${parameters}", paramsJson);
    }

    private String inlineParameters(String sql, List<QueryRequest.Parameter> parameters) {
        String result = sql;
        for (QueryRequest.Parameter p : parameters) {
            String replacement = formatParameterValue(p);
            result = result.replaceFirst("\\?", replacement);
        }
        return result;
    }

    private String formatParameterValue(QueryRequest.Parameter p) {
        if (p == null || p.value() == null) return "NULL";
        Object v = p.value();
        return switch (p.type()) {
            case "string", "date", "time", "timestamp" -> "'" + v.toString().replace("'", "''") + "'";
            case "boolean" -> ((Boolean) v) ? "TRUE" : "FALSE";
            default -> v.toString();
        };
    }

    private QueryResult parseNdjsonResponse(InputStream is) throws IOException, SQLException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String firstLine = reader.readLine();
            if (firstLine == null || firstLine.isEmpty()) {
                return new QueryResult(null, null, 0);
            }

            // Check for error
            try {
                String error = JsonPath.read(firstLine, schema.errorPath);
                if (error != null) throw new SQLException(error);
            } catch (PathNotFoundException ignored) {}

            // Check for update count
            try {
                Object updateCount = JsonPath.read(firstLine, schema.updateCountPath);
                if (updateCount instanceof Number) {
                    return new QueryResult(null, null, ((Number) updateCount).intValue());
                }
            } catch (PathNotFoundException ignored) {}

            // Parse columns
            List<Map<String, String>> columnDefs = JsonPath.read(firstLine, schema.columnsPath);
            List<String> columns = columnDefs.stream().map(c -> c.get(schema.columnNameField)).toList();

            // Parse rows
            List<List<Object>> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (schema.rowsAsObjects) {
                    Map<String, Object> rowMap = objectMapper.readValue(line, Map.class);
                    rows.add(columns.stream().map(rowMap::get).toList());
                } else {
                    rows.add(objectMapper.readValue(line, List.class));
                }
            }
            return new QueryResult(columns, rows, -1);
        }
    }

    private QueryResult parseJsonResponse(InputStream is) throws IOException, SQLException {
        String body = readFully(is);
        if (body == null || body.isEmpty()) {
            return new QueryResult(null, null, 0);
        }

        // Check for error
        try {
            String error = JsonPath.read(body, schema.errorPath);
            if (error != null) throw new SQLException(error);
        } catch (PathNotFoundException ignored) {}

        // Try to parse columns first
        List<String> columns = null;
        try {
            List<Map<String, String>> columnDefs = JsonPath.read(body, schema.columnsPath);
            columns = columnDefs.stream().map(c -> c.get(schema.columnNameField)).toList();
        } catch (PathNotFoundException ignored) {}

        // If no columns, check for update count
        if (columns == null || columns.isEmpty()) {
            try {
                Object updateCount = JsonPath.read(body, schema.updateCountPath);
                if (updateCount instanceof Number) {
                    return new QueryResult(null, null, ((Number) updateCount).intValue());
                }
            } catch (PathNotFoundException ignored) {}
            return new QueryResult(null, null, 0);
        }

        // Parse rows
        List<List<Object>> rows = new ArrayList<>();
        if (schema.rowsAsObjects) {
            List<Map<String, Object>> rowMaps = JsonPath.read(body, schema.rowsPath);
            for (Map<String, Object> rowMap : rowMaps) {
                rows.add(columns.stream().map(rowMap::get).toList());
            }
        } else {
            rows = JsonPath.read(body, schema.rowsPath);
        }
        return new QueryResult(columns, rows, -1);
    }

    private String readFully(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString().trim();
        }
    }

    // Remaining Connection interface methods
    @Override public Statement createStatement(int t, int c) throws SQLException { return createStatement(); }
    @Override public PreparedStatement prepareStatement(String sql, int t, int c) throws SQLException { return prepareStatement(sql); }
    @Override public CallableStatement prepareCall(String sql, int t, int c) throws SQLException { throw new SQLFeatureNotSupportedException("CallableStatement not supported"); }
    @Override public Map<String, Class<?>> getTypeMap() { return new HashMap<>(); }
    @Override public void setTypeMap(Map<String, Class<?>> map) {}
    @Override public void setHoldability(int h) {}
    @Override public int getHoldability() { return ResultSet.HOLD_CURSORS_OVER_COMMIT; }
    @Override public Savepoint setSavepoint() throws SQLException { throw new SQLFeatureNotSupportedException("Savepoints not supported"); }
    @Override public Savepoint setSavepoint(String name) throws SQLException { throw new SQLFeatureNotSupportedException("Savepoints not supported"); }
    @Override public void rollback(Savepoint sp) throws SQLException { throw new SQLFeatureNotSupportedException("Savepoints not supported"); }
    @Override public void releaseSavepoint(Savepoint sp) throws SQLException { throw new SQLFeatureNotSupportedException("Savepoints not supported"); }
    @Override public Statement createStatement(int t, int c, int h) throws SQLException { return createStatement(); }
    @Override public PreparedStatement prepareStatement(String sql, int t, int c, int h) throws SQLException { return prepareStatement(sql); }
    @Override public CallableStatement prepareCall(String sql, int t, int c, int h) throws SQLException { throw new SQLFeatureNotSupportedException("CallableStatement not supported"); }
    @Override public PreparedStatement prepareStatement(String sql, int k) throws SQLException { return prepareStatement(sql); }
    @Override public PreparedStatement prepareStatement(String sql, int[] idx) throws SQLException { return prepareStatement(sql); }
    @Override public PreparedStatement prepareStatement(String sql, String[] names) throws SQLException { return prepareStatement(sql); }
    @Override public Clob createClob() throws SQLException { throw new SQLFeatureNotSupportedException("Clob not supported"); }
    @Override public Blob createBlob() throws SQLException { throw new SQLFeatureNotSupportedException("Blob not supported"); }
    @Override public NClob createNClob() throws SQLException { throw new SQLFeatureNotSupportedException("NClob not supported"); }
    @Override public SQLXML createSQLXML() throws SQLException { throw new SQLFeatureNotSupportedException("SQLXML not supported"); }
    @Override public boolean isValid(int timeout) { return !closed; }
    @Override public void setClientInfo(String name, String value) {}
    @Override public void setClientInfo(Properties props) {}
    @Override public String getClientInfo(String name) { return null; }
    @Override public Properties getClientInfo() { return new Properties(); }
    @Override public Array createArrayOf(String typeName, Object[] elements) throws SQLException { throw new SQLFeatureNotSupportedException("Arrays not supported"); }
    @Override public Struct createStruct(String typeName, Object[] attrs) throws SQLException { throw new SQLFeatureNotSupportedException("Structs not supported"); }
    @Override public void setSchema(String schema) {}
    @Override public String getSchema() { return null; }
    @Override public void abort(Executor executor) { closed = true; }
    @Override public void setNetworkTimeout(Executor executor, int ms) {}
    @Override public int getNetworkTimeout() { return 0; }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { throw new SQLException("Cannot unwrap to " + iface.getName()); }
    @Override public boolean isWrapperFor(Class<?> iface) { return false; }
}
