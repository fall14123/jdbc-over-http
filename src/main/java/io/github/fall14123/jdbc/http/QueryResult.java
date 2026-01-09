package io.github.fall14123.jdbc.http;

import java.util.List;

public class QueryResult {
    private List<String> columns;
    private List<List<Object>> rows;
    private int updateCount;

    public QueryResult() {
    }

    public QueryResult(List<String> columns, List<List<Object>> rows, int updateCount) {
        this.columns = columns;
        this.rows = rows;
        this.updateCount = updateCount;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public void setRows(List<List<Object>> rows) {
        this.rows = rows;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }
}