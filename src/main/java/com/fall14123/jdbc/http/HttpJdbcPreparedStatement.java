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
    private final String originalSql;
    private final List<Object> parameters = new ArrayList<>();

    public HttpJdbcPreparedStatement(HttpJdbcConnection connection, String sql) {
        super(connection);
        this.originalSql = sql;
        
        int paramCount = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                paramCount++;
            }
        }
        
        for (int i = 0; i < paramCount; i++) {
            parameters.add(null);
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return super.executeQuery(buildSql());
    }

    @Override
    public int executeUpdate() throws SQLException {
        return super.executeUpdate(buildSql());
    }

    @Override
    public boolean execute() throws SQLException {
        return super.execute(buildSql());
    }

    private String buildSql() {
        String sql = originalSql;
        for (int i = parameters.size() - 1; i >= 0; i--) {
            Object param = parameters.get(i);
            String paramStr = param == null ? "NULL" : formatParameter(param);
            sql = sql.replaceFirst("\\?", paramStr);
        }
        return sql;
    }

    private String formatParameter(Object param) {
        if (param == null) {
            return "NULL";
        } else if (param instanceof String) {
            return "'" + ((String) param).replace("'", "''") + "'";
        } else if (param instanceof Number) {
            return param.toString();
        } else if (param instanceof Boolean) {
            return ((Boolean) param) ? "TRUE" : "FALSE";
        } else if (param instanceof Date || param instanceof Time || param instanceof Timestamp) {
            return "'" + param.toString() + "'";
        } else {
            return "'" + param.toString().replace("'", "''") + "'";
        }
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setParameter(parameterIndex, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    private void setParameter(int parameterIndex, Object value) throws SQLException {
        if (parameterIndex < 1 || parameterIndex > parameters.size()) {
            throw new SQLException("Parameter index out of range: " + parameterIndex);
        }
        parameters.set(parameterIndex - 1, value);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setAsciiStream not supported");
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setUnicodeStream not supported");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBinaryStream not supported");
    }

    @Override
    public void clearParameters() throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            parameters.set(i, null);
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void addBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException("Batch updates not supported");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setCharacterStream not supported");
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRef not supported");
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBlob not supported");
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setClob not supported");
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setArray not supported");
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("getMetaData not supported");
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setParameter(parameterIndex, null);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        setParameter(parameterIndex, x.toString());
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("getParameterMetaData not supported");
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRowId not supported");
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setParameter(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNCharacterStream not supported");
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob not supported");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setClob not supported");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBlob not supported");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob not supported");
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("setSQLXML not supported");
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        setParameter(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setAsciiStream not supported");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBinaryStream not supported");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setCharacterStream not supported");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setAsciiStream not supported");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBinaryStream not supported");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("setCharacterStream not supported");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNCharacterStream not supported");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("setClob not supported");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBlob not supported");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob not supported");
    }
}