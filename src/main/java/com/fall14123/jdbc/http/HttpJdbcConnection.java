package com.fall14123.jdbc.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;
import javax.net.ssl.HttpsURLConnection;

public class HttpJdbcConnection implements Connection {
    private final URL serverUrl;
    private final String username;
    private final String password;
    private final ObjectMapper objectMapper;
    private boolean closed = false;
    private boolean autoCommit = true;

    public HttpJdbcConnection(URL serverUrl, String username, String password, ObjectMapper objectMapper) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.objectMapper = objectMapper;
    }

    @Override
    public Statement createStatement() throws SQLException {
        checkClosed();
        return new HttpJdbcStatement(this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkClosed();
        return new HttpJdbcPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatement not supported");
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return sql;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkClosed();
        this.autoCommit = autoCommit;
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        checkClosed();
        return autoCommit;
    }

    @Override
    public void commit() throws SQLException {
        checkClosed();
        if (autoCommit) {
            throw new SQLException("Cannot commit when auto-commit is enabled");
        }
    }

    @Override
    public void rollback() throws SQLException {
        checkClosed();
        if (autoCommit) {
            throw new SQLException("Cannot rollback when auto-commit is enabled");
        }
    }

    @Override
    public void close() throws SQLException {
        closed = true;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        checkClosed();
        return new HttpJdbcDatabaseMetaData(this);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        checkClosed();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        checkClosed();
        return false;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        checkClosed();
    }

    @Override
    public String getCatalog() throws SQLException {
        checkClosed();
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        checkClosed();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        checkClosed();
        return Connection.TRANSACTION_NONE;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkClosed();
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        checkClosed();
    }

    private void checkClosed() throws SQLException {
        if (closed) {
            throw new SQLException("Connection is closed");
        }
    }

    public QueryResult executeQuery(String sql) throws SQLException {
        try {
            HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setDoOutput(true);

            if (username != null && !username.isEmpty()) {
                String auth = username + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            }

            try (OutputStream os = connection.getOutputStream()) {
                os.write(sql.getBytes());
            }

            int responseCode = connection.getResponseCode();
            String responseBody;

            if (responseCode == 200) {
                try (InputStream is = connection.getInputStream()) {
                    responseBody = readInputStream(is);
                }
                
                System.out.println("DEBUG: Raw response body: '" + responseBody + "'");
                
                try {
                    QueryResult result = objectMapper.readValue(responseBody, QueryResult.class);
                    System.out.println("DEBUG: Deserialized as QueryResult - columns: " + 
                                     (result.getColumns() != null ? result.getColumns().size() : "null") +
                                     ", updateCount: " + result.getUpdateCount());
                    return result;
                } catch (Exception e) {
                    System.out.println("DEBUG: Failed to deserialize as QueryResult, trying generic parsing");
                    return parseGenericResponse(responseBody);
                }
            } else {
                try (InputStream es = connection.getErrorStream()) {
                    responseBody = readInputStream(es);
                }
                
                try {
                    ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
                    throw new SQLException(error.getError());
                } catch (Exception e) {
                    throw new SQLException("Server error: " + responseBody);
                }
            }

        } catch (IOException e) {
            throw new SQLException("Failed to execute query over HTTP", e);
        }
    }

    private QueryResult parseGenericResponse(String responseBody) throws SQLException {
        try {
            // Handle empty response body (common for DDL/DML operations)
            if (responseBody == null || responseBody.trim().isEmpty()) {
                return new QueryResult(null, null, 0);
            }
            
            // Debug: print response body to understand what we're getting
            System.out.println("DEBUG: Response body: '" + responseBody + "'");
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> jsonMap = objectMapper.readValue(responseBody, java.util.Map.class);
            
            // Check if this looks like an update result (has count/affected_rows etc.)
            // Check both lowercase and capitalized versions
            String[] countKeys = {"count", "Count", "affected_rows", "Affected_Rows", 
                                "changes", "Changes", "rows_affected", "Rows_Affected"};
            
            Object countValue = null;
            for (String key : countKeys) {
                if (jsonMap.containsKey(key)) {
                    countValue = jsonMap.get(key);
                    break;
                }
            }
            
            if (countValue != null) {
                // This is an update/insert result, return it as an update count
                int updateCount = 0;
                if (countValue instanceof Number) {
                    updateCount = ((Number) countValue).intValue();
                } else if (countValue instanceof String) {
                    try {
                        updateCount = Integer.parseInt((String) countValue);
                    } catch (NumberFormatException e) {
                        updateCount = 0;
                    }
                }
                System.out.println("DEBUG: Detected update operation with count: " + updateCount);
                return new QueryResult(null, null, updateCount);
            }
            
            java.util.List<String> columns = new java.util.ArrayList<>(jsonMap.keySet());
            java.util.List<Object> rowValues = new java.util.ArrayList<>(jsonMap.values());
            java.util.List<java.util.List<Object>> rows = new java.util.ArrayList<>();
            rows.add(rowValues);
            
            return new QueryResult(columns, rows, -1);
        } catch (Exception e) {
            throw new SQLException("Failed to parse server response: " + responseBody, e);
        }
    }

    private String readInputStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatement not supported");
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return new HashMap<>();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
    }

    @Override
    public int getHoldability() throws SQLException {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints not supported");
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints not supported");
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints not supported");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints not supported");
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatement not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("Clob not supported");
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLFeatureNotSupportedException("Blob not supported");
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob not supported");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException("SQLXML not supported");
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return !closed;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return new Properties();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLFeatureNotSupportedException("Arrays not supported");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLFeatureNotSupportedException("Structs not supported");
    }

    @Override
    public void setSchema(String schema) throws SQLException {
    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        close();
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}