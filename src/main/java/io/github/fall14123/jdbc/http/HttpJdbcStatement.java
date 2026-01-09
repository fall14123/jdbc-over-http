package io.github.fall14123.jdbc.http;

import java.sql.*;
import java.util.List;

public class HttpJdbcStatement implements Statement {
    protected final HttpJdbcConnection connection;
    private final HttpJdbcLogger logger;
    protected ResultSet currentResultSet;
    protected int currentUpdateCount = -1;
    private boolean closed = false;

    public HttpJdbcStatement(HttpJdbcConnection connection, LogLevel logLevel) {
        this.connection = connection;
        this.logger = new HttpJdbcLogger("HttpJdbcStatement", logLevel);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        checkClosed();
        QueryResult result = connection.executeQuery(sql);
        if (result.getColumns() != null) {
            currentResultSet = new HttpJdbcResultSet(result);
            currentUpdateCount = -1;
            return currentResultSet;
        }
        throw new SQLException("Query did not return a result set");
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        checkClosed();
        QueryResult result = connection.executeQuery(sql);
        // Check if this looks like a DML result (single "Count" column)
        if (result.getColumns() != null && result.getColumns().size() == 1 
            && result.getColumns().get(0).equalsIgnoreCase("Count")
            && result.getRows() != null && result.getRows().size() == 1) {
            Object count = result.getRows().get(0).get(0);
            currentUpdateCount = count instanceof Number ? ((Number) count).intValue() : 0;
            currentResultSet = null;
            return currentUpdateCount;
        }
        // Allow empty result sets (DDL returns columns but no rows)
        if (result.getColumns() != null && result.getRows() != null && !result.getRows().isEmpty()) {
            throw new SQLException("Query returned a result set, use executeQuery instead");
        }
        currentUpdateCount = result.getUpdateCount() >= 0 ? result.getUpdateCount() : 0;
        currentResultSet = null;
        return currentUpdateCount;
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        checkClosed();
        QueryResult result = connection.executeQuery(sql);
        if (result.getColumns() != null) {
            currentResultSet = new HttpJdbcResultSet(result);
            currentUpdateCount = -1;
            return true;
        }
        currentUpdateCount = result.getUpdateCount();
        currentResultSet = null;
        return false;
    }

    @Override public void close() throws SQLException { if (currentResultSet != null) currentResultSet.close(); closed = true; }
    @Override public ResultSet getResultSet() throws SQLException { checkClosed(); return currentResultSet; }
    @Override public int getUpdateCount() throws SQLException { checkClosed(); return currentUpdateCount; }
    @Override public Connection getConnection() { return connection; }
    @Override public boolean isClosed() { return closed; }
    @Override public boolean getMoreResults() throws SQLException { checkClosed(); return false; }
    @Override public boolean getMoreResults(int current) { return false; }

    private void checkClosed() throws SQLException { if (closed) throw new SQLException("Statement is closed"); }

    // Stub implementations
    @Override public int getMaxFieldSize() { return 0; }
    @Override public void setMaxFieldSize(int max) {}
    @Override public int getMaxRows() { return 0; }
    @Override public void setMaxRows(int max) {}
    @Override public void setEscapeProcessing(boolean enable) {}
    @Override public int getQueryTimeout() { return 0; }
    @Override public void setQueryTimeout(int seconds) {}
    @Override public void cancel() throws SQLException { throw new SQLFeatureNotSupportedException("Cancel not supported"); }
    @Override public SQLWarning getWarnings() { return null; }
    @Override public void clearWarnings() {}
    @Override public void setCursorName(String name) throws SQLException { throw new SQLFeatureNotSupportedException("Named cursors not supported"); }
    @Override public void setFetchDirection(int direction) {}
    @Override public int getFetchDirection() { return ResultSet.FETCH_FORWARD; }
    @Override public void setFetchSize(int rows) {}
    @Override public int getFetchSize() { return 0; }
    @Override public int getResultSetConcurrency() { return ResultSet.CONCUR_READ_ONLY; }
    @Override public int getResultSetType() { return ResultSet.TYPE_FORWARD_ONLY; }
    @Override public void addBatch(String sql) throws SQLException { throw new SQLFeatureNotSupportedException("Batch updates not supported"); }
    @Override public void clearBatch() throws SQLException { throw new SQLFeatureNotSupportedException("Batch updates not supported"); }
    @Override public int[] executeBatch() throws SQLException { throw new SQLFeatureNotSupportedException("Batch updates not supported"); }
    @Override public ResultSet getGeneratedKeys() throws SQLException { throw new SQLFeatureNotSupportedException("Generated keys not supported"); }
    @Override public int executeUpdate(String sql, int k) throws SQLException { return executeUpdate(sql); }
    @Override public int executeUpdate(String sql, int[] idx) throws SQLException { return executeUpdate(sql); }
    @Override public int executeUpdate(String sql, String[] names) throws SQLException { return executeUpdate(sql); }
    @Override public boolean execute(String sql, int k) throws SQLException { return execute(sql); }
    @Override public boolean execute(String sql, int[] idx) throws SQLException { return execute(sql); }
    @Override public boolean execute(String sql, String[] names) throws SQLException { return execute(sql); }
    @Override public int getResultSetHoldability() { return ResultSet.HOLD_CURSORS_OVER_COMMIT; }
    @Override public void setPoolable(boolean poolable) {}
    @Override public boolean isPoolable() { return false; }
    @Override public void closeOnCompletion() {}
    @Override public boolean isCloseOnCompletion() { return false; }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { throw new SQLException("Cannot unwrap to " + iface.getName()); }
    @Override public boolean isWrapperFor(Class<?> iface) { return false; }
}
