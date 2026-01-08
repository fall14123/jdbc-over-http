package com.fall14123.jdbc.http;

import java.util.List;

public record QueryRequest(String sql, List<Parameter> parameters) {
    public record Parameter(Object value, String type) {}
}
