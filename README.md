# JDBC over HTTP

A JDBC driver that allows database access over HTTP/HTTPS connections. Supports multiple backend APIs through configurable schema mappings.

## Features

- JDBC 4.0 compatible driver
- HTTP and HTTPS support
- Multiple backend support via schema configuration (Flock, DuckDB httpserver, custom)
- JSONPath-based response parsing for easy adaptation to different APIs
- Support for Statement and PreparedStatement
- Compatible with Java 21 (Amazon Corretto)

## Quick Start

### 1. Build the Project

```bash
./gradlew build

# Build standalone jar with all dependencies
./gradlew uberjar
```

### 2. Use as JDBC Driver

```java
import java.sql.*;
import java.util.Properties;

public class Example {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        props.setProperty("schema", "httpserver");  // or "flock" (default)
        
        try (Connection conn = DriverManager.getConnection(url, props)) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 'hello' as greeting, version() as version");
                while (rs.next()) {
                    System.out.println(rs.getString("greeting"));
                    System.out.println(rs.getString("version"));
                }
            }
        }
    }
}
```

## URL Formats

- HTTP: `jdbc:http://hostname:port/path`
- HTTPS: `jdbc:https://hostname:port/path`

## Schema Configuration

The driver supports multiple backend APIs via schema configuration.

### Selecting a Schema

```java
// Via connection property
props.setProperty("schema", "httpserver");  // or "flock" (default)

// Or via system property
// -Djdbc.http.schema=httpserver
```

### Built-in Schemas

| Schema | Description | Use Case |
|--------|-------------|----------|
| `flock` | JSON request, NDJSON response (default) | Flock service at `localhost:8080/v1/query` |
| `httpserver` | Plain text SQL, JSON response | DuckDB httpserver extension at `localhost:9999` |

### Schema Configuration Options

| Property | Description |
|----------|-------------|
| **Request** | |
| `request.contentType` | HTTP Content-Type header (`application/json` or `text/plain`) |
| `request.template` | Request body template. Variables: `${sql}`, `${parameters}` |
| `request.parameterTemplate` | Template for each parameter. Variables: `${value}`, `${type}`. If empty, parameters are inlined into SQL |
| `request.urlSuffix` | Appended to URL (e.g., `?default_format=JSONCompact`) |
| **Response** | |
| `response.ndjson` | `true` for streaming NDJSON, `false` for single JSON |
| `response.columnsPath` | JSONPath to column definitions array |
| `response.columnNameField` | Field name for column name |
| `response.columnTypeField` | Field name for column type |
| `response.errorPath` | JSONPath to error message |
| `response.updateCountPath` | JSONPath to update count |
| `response.rowsAsObjects` | `true` if rows are objects `{"col": "val"}`, `false` if arrays `["val"]` |
| `response.rowsPath` | JSONPath to rows array (non-NDJSON only) |

### Custom Schema

Create `schemas/myschema.properties` on the classpath:

```properties
# Request
request.contentType=application/json
request.template={"sql": "${sql}", "parameters": ${parameters}}
request.parameterTemplate={"value": ${value}, "type": "${type}"}
request.urlSuffix=

# Response (uses JSONPath)
response.ndjson=true
response.columnsPath=$._meta.columns[*]
response.columnNameField=name
response.columnTypeField=type
response.errorPath=$.error
response.updateCountPath=$.updateCount
response.rowsAsObjects=true
response.rowsPath=$.data[*]
```

Then use: `props.setProperty("schema", "myschema")`

## Connection Properties

| Property | Default | Description |
|----------|---------|-------------|
| `user` | | Username for HTTP Basic Auth |
| `password` | | Password for HTTP Basic Auth |
| `schema` | `flock` | Schema configuration name |
| `connectTimeout` | `30000` | Connection timeout in ms |
| `readTimeout` | `60000` | Read timeout in ms |
| `keepAlive` | `true` | Use HTTP keep-alive |
| `logLevel` | `INFO` | Logging level |

## Running Tests

```bash
# Run all tests
./gradlew test

# Run httpserver tests (requires DuckDB httpserver on localhost:9999)
./gradlew test --tests HttpServerSchemaTest

# Run flock tests (requires Flock on localhost:8080)
./gradlew test --tests FlockSchemaTest
```

## Project Structure

```
src/
├── main/
│   ├── java/com/fall14123/jdbc/http/
│   │   ├── HttpJdbcDriver.java           # Driver entry point
│   │   ├── HttpJdbcConnection.java       # Connection & query execution
│   │   ├── HttpJdbcStatement.java        # Statement implementation
│   │   ├── HttpJdbcPreparedStatement.java # PreparedStatement
│   │   ├── HttpJdbcResultSet.java        # ResultSet implementation
│   │   ├── SchemaConfig.java             # Schema configuration loader
│   │   ├── QueryRequest.java             # Request model
│   │   └── QueryResult.java              # Response model
│   └── resources/schemas/
│       ├── flock.properties              # Flock schema config
│       └── httpserver.properties         # DuckDB httpserver config
└── test/java/com/fall14123/jdbc/http/
    ├── FlockSchemaTest.java              # Flock integration tests
    └── HttpServerSchemaTest.java         # httpserver integration tests
```

## Limitations

- No transaction support (auto-commit only)
- No stored procedures / CallableStatement
- No batch updates
- No BLOB/CLOB support
- Limited metadata support

## License

Apache License 2.0
