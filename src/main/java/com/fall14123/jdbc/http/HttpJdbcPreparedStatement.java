package com.fall14123.jdbc.http;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HttpJdbcPreparedStatement extends HttpJdbcStatement implements PreparedStatement {
    private final String sql;
    private final List<QueryRequest.Parameter> parameters;

    public HttpJdbcPreparedStatement(HttpJdbcConnection connection, String sql, LogLevel logLevel) {
        super(connection, logLevel);
        this.sql = sql;
        int count = (int) sql.chars().filter(c -> c == '?').count();
        this.parameters = new ArrayList<>(count);
        for (int i = 0; i < count; i++) parameters.add(null);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        QueryResult result = connection.executeQuery(sql, parameters);
        currentResultSet = new HttpJdbcResultSet(result);
        return currentResultSet;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return connection.executeQuery(sql, parameters).getUpdateCount();
    }

    @Override
    public boolean execute() throws SQLException {
        QueryResult result = connection.executeQuery(sql, parameters);
        if (result.getColumns() != null) {
            currentResultSet = new HttpJdbcResultSet(result);
            return true;
        }
        currentUpdateCount = result.getUpdateCount();
        return false;
    }

    private void setParam(int i, Object v, String t) throws SQLException {
        if (i < 1 || i > parameters.size()) throw new SQLException("Parameter index out of range: " + i);
        parameters.set(i - 1, new QueryRequest.Parameter(v, t));
    }

    @Override public void setNull(int i, int t) throws SQLException { setParam(i, null, "null"); }
    @Override public void setBoolean(int i, boolean x) throws SQLException { setParam(i, x, "boolean"); }
    @Override public void setByte(int i, byte x) throws SQLException { setParam(i, x, "byte"); }
    @Override public void setShort(int i, short x) throws SQLException { setParam(i, x, "short"); }
    @Override public void setInt(int i, int x) throws SQLException { setParam(i, x, "int"); }
    @Override public void setLong(int i, long x) throws SQLException { setParam(i, x, "long"); }
    @Override public void setFloat(int i, float x) throws SQLException { setParam(i, x, "float"); }
    @Override public void setDouble(int i, double x) throws SQLException { setParam(i, x, "double"); }
    @Override public void setBigDecimal(int i, BigDecimal x) throws SQLException { setParam(i, x != null ? x.toString() : null, "decimal"); }
    @Override public void setString(int i, String x) throws SQLException { setParam(i, x, "string"); }
    @Override public void setBytes(int i, byte[] x) throws SQLException { setParam(i, x != null ? java.util.Base64.getEncoder().encodeToString(x) : null, "bytes"); }
    @Override public void setDate(int i, Date x) throws SQLException { setParam(i, x != null ? x.toString() : null, "date"); }
    @Override public void setTime(int i, Time x) throws SQLException { setParam(i, x != null ? x.toString() : null, "time"); }
    @Override public void setTimestamp(int i, Timestamp x) throws SQLException { setParam(i, x != null ? x.toString() : null, "timestamp"); }
    @Override public void setDate(int i, Date x, Calendar c) throws SQLException { setDate(i, x); }
    @Override public void setTime(int i, Time x, Calendar c) throws SQLException { setTime(i, x); }
    @Override public void setTimestamp(int i, Timestamp x, Calendar c) throws SQLException { setTimestamp(i, x); }
    @Override public void setObject(int i, Object x) throws SQLException { setParam(i, x, "object"); }
    @Override public void setObject(int i, Object x, int t) throws SQLException { setObject(i, x); }
    @Override public void setObject(int i, Object x, int t, int s) throws SQLException { setObject(i, x); }
    @Override public void setNull(int i, int t, String n) throws SQLException { setNull(i, t); }
    @Override public void setURL(int i, URL x) throws SQLException { setParam(i, x != null ? x.toString() : null, "string"); }
    @Override public void setNString(int i, String x) throws SQLException { setString(i, x); }
    @Override public void clearParameters() { for (int i = 0; i < parameters.size(); i++) parameters.set(i, null); }

    // Unsupported operations
    @Override public void setAsciiStream(int i, InputStream x, int len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setUnicodeStream(int i, InputStream x, int len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setBinaryStream(int i, InputStream x, int len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setCharacterStream(int i, Reader r, int len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setRef(int i, Ref x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setBlob(int i, Blob x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setClob(int i, Clob x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setArray(int i, Array x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public ResultSetMetaData getMetaData() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public ParameterMetaData getParameterMetaData() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setRowId(int i, RowId x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setNCharacterStream(int i, Reader r, long len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setNClob(int i, NClob x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setClob(int i, Reader r, long len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setBlob(int i, InputStream s, long len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setNClob(int i, Reader r, long len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setSQLXML(int i, SQLXML x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setAsciiStream(int i, InputStream x, long len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setBinaryStream(int i, InputStream x, long len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setCharacterStream(int i, Reader r, long len) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setAsciiStream(int i, InputStream x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setBinaryStream(int i, InputStream x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setCharacterStream(int i, Reader r) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setNCharacterStream(int i, Reader r) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setClob(int i, Reader r) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setBlob(int i, InputStream s) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void setNClob(int i, Reader r) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void addBatch() throws SQLException { throw new SQLFeatureNotSupportedException(); }
}
