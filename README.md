# JDBC over HTTP

A JDBC driver wrapper that allows database access over HTTP/HTTPS connections.

## Features

- Complete JDBC 4.0 compatible driver implementation
- HTTP and HTTPS support with optional SSL configuration  
- Connection pooling and credential management
- Support for both Statement and PreparedStatement
- JSON-based communication protocol
- Compatible with Java 21 (Amazon Corretto)

## Quick Start

### 1. Build the Project

```bash
./gradlew build

# Build standalone jar with all dependencies
./gradlew uberjar
```

### 2. Test with Curl

Assuming your HTTP server is running on localhost:9999:

```bash
curl -X POST -d "SELECT 'hello', version()" "http://user:pass@localhost:9999/"
```

### 3. Use as JDBC Driver

```java
import java.sql.*;
import java.util.Properties;

public class Example {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:http://localhost:9999/";
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "pass");
        
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

- HTTP: `jdbc:http://hostname:port/`
- HTTPS: `jdbc:https://hostname:port/`

## SSL Configuration

For HTTPS connections, the server must be configured with SSL certificates. The driver will automatically use HTTPS when the URL scheme is `https://`.

## Running Tests

```bash
# Run unit tests
./gradlew test

# Run example client
./gradlew run

# Or run the standalone jar
java -jar build/libs/jdbc-over-http-1.0.0-all.jar
```

## API Protocol

The driver communicates with the HTTP server using a simple REST API:

- **Method**: POST
- **URL**: `/`
- **Content-Type**: `text/plain`
- **Body**: SQL query string
- **Authentication**: HTTP Basic Authentication

### Response Format

#### Success Response (200 OK)
```json
{
  "columns": ["col1", "col2"],
  "rows": [["value1", "value2"], ["value3", "value4"]],
  "updateCount": -1
}
```

#### Update Response (200 OK)
```json
{
  "columns": null,
  "rows": null,
  "updateCount": 5
}
```

#### Error Response (500 Internal Server Error)
```json
{
  "error": "Error message"
}
```

## Development

### Project Structure

```
src/
├── main/java/com/fall14123/jdbc/http/
│   ├── HttpJdbcDriver.java           # Main driver class
│   ├── HttpJdbcConnection.java       # Connection implementation
│   ├── HttpJdbcStatement.java        # Statement implementation
│   ├── HttpJdbcPreparedStatement.java # PreparedStatement implementation
│   ├── HttpJdbcResultSet.java        # ResultSet implementation
│   ├── HttpJdbcResultSetMetaData.java # ResultSet metadata
│   ├── HttpJdbcDatabaseMetaData.java # Database metadata
│   ├── QueryResult.java              # Query result data structure
│   ├── ErrorResponse.java            # Error response data structure
│   └── HttpJdbcClientExample.java    # Example usage
└── test/java/com/fall14123/jdbc/http/
    └── HttpJdbcClientTest.java       # Unit tests
```

### Building from Source

1. Ensure Java 21 is installed
2. Clone the repository
3. Run `./gradlew build`

### Key Classes

- **HttpJdbcDriver**: Main entry point, registers with DriverManager
- **HttpJdbcConnection**: Manages HTTP connections and executes queries
- **HttpJdbcStatement**: Handles basic SQL statements
- **HttpJdbcPreparedStatement**: Handles parameterized queries
- **HttpJdbcResultSet**: Provides access to query results

## Limitations

- No support for transactions (auto-commit only)
- No support for stored procedures or callable statements
- No support for batch updates
- No support for binary data types (BLOB/CLOB)
- Limited metadata support

## License

This project is licensed under the Apache License 2.0.