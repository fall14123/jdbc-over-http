# JDBC-over-HTTP Configuration

This JDBC driver supports configurable logging levels and HTTP timeouts to help with debugging, monitoring, and performance tuning.

## Log Levels

The driver supports the following log levels (from least to most verbose):

- `ERROR` - Only error messages
- `WARN` - Warnings and errors
- `INFO` - General information, warnings, and errors (default)
- `DEBUG` - Debug information including HTTP requests/responses
- `TRACE` - Most detailed logging

## Configuration Options

### 1. System Property

Set the log level globally using a system property:

```bash
java -Djdbc.http.log.level=DEBUG MyApplication
```

### 2. Connection Properties

Set the log level per connection using connection properties:

```java
Properties props = new Properties();
props.setProperty("user", "myuser");
props.setProperty("password", "mypass");
props.setProperty("logLevel", "DEBUG");  // Set log level here

Connection conn = DriverManager.getConnection("jdbc:http://localhost:9999/", props);
```

### 3. URL Parameters

You can also include the log level in the JDBC URL:

```java
String url = "jdbc:http://localhost:9999/?logLevel=DEBUG";
Connection conn = DriverManager.getConnection(url, props);
```

## Log Output Format

Log messages are formatted as:
```
[LEVEL] ComponentName: Message
```

Examples:
```
[DEBUG] HttpJdbcConnection: Raw response body: '{"greeting":"hello"}'
[DEBUG] HttpJdbcStatement: executeUpdate called with SQL: CREATE TABLE test (id INT)
[INFO] HttpJdbcConnection: Connected to http://localhost:9999/
```

## Common Use Cases

### Production Deployment
Use `ERROR` or `WARN` level:
```java
props.setProperty("logLevel", "WARN");
```

### Development and Testing
Use `DEBUG` level to see HTTP requests and responses:
```java
props.setProperty("logLevel", "DEBUG");
```

### Troubleshooting
Use `TRACE` level for maximum detail:
```java
props.setProperty("logLevel", "TRACE");
```

## Default Behavior

If no log level is specified, the driver defaults to `INFO` level.

---

# HTTP Timeout Configuration

The driver supports configurable HTTP connection and read timeouts to handle network latency and server response times.

## Timeout Types

- **Connection Timeout**: Maximum time to wait when establishing a connection to the server
- **Read Timeout**: Maximum time to wait for data to be received from the server

## Default Values

- **Connection Timeout**: 30,000ms (30 seconds)
- **Read Timeout**: 60,000ms (60 seconds)

## Configuration Options

### 1. System Properties

Set timeouts globally using system properties:

```bash
java -Djdbc.http.connect.timeout=15000 -Djdbc.http.read.timeout=30000 MyApplication
```

### 2. Connection Properties

Set timeouts per connection using connection properties:

```java
Properties props = new Properties();
props.setProperty("user", "myuser");
props.setProperty("password", "mypass");
props.setProperty("connectTimeout", "15000");  // 15 seconds
props.setProperty("readTimeout", "30000");     // 30 seconds

Connection conn = DriverManager.getConnection("jdbc:http://localhost:9999/", props);
```

### 3. Combined Configuration Example

Configure both logging and timeouts together:

```java
Properties props = new Properties();
props.setProperty("user", "myuser");
props.setProperty("password", "mypass");
props.setProperty("logLevel", "DEBUG");
props.setProperty("connectTimeout", "10000");  // 10 seconds
props.setProperty("readTimeout", "45000");     // 45 seconds

Connection conn = DriverManager.getConnection("jdbc:http://localhost:9999/", props);
```

## Common Timeout Scenarios

### Fast Local Network
```java
props.setProperty("connectTimeout", "5000");   // 5 seconds
props.setProperty("readTimeout", "15000");     // 15 seconds
```

### Slow Network/Internet
```java
props.setProperty("connectTimeout", "30000");  // 30 seconds
props.setProperty("readTimeout", "120000");    // 2 minutes
```

### Long-Running Queries
```java
props.setProperty("connectTimeout", "15000");  // 15 seconds
props.setProperty("readTimeout", "300000");    // 5 minutes
```

## Error Handling

- **SocketTimeoutException**: Thrown when connection timeout is exceeded
- **SocketTimeoutException**: Thrown when read timeout is exceeded while waiting for server response
- Values must be positive integers (milliseconds)
- Invalid values fall back to defaults

---

# HTTP Keep-Alive Configuration

The driver supports HTTP keep-alive connections to improve performance by reusing connections for multiple requests.

## Keep-Alive Benefits

- **Reduced Latency**: Eliminates TCP handshake overhead for subsequent requests
- **Better Performance**: Reuses existing connections instead of creating new ones
- **Server Efficiency**: Reduces server connection overhead

## Default Behavior

Keep-alive is **enabled by default** (recommended for most use cases).

## Configuration Options

### 1. System Property

```bash
java -Djdbc.http.keep.alive=false MyApplication  # Disable keep-alive
```

### 2. Connection Property

```java
Properties props = new Properties();
props.setProperty("keepAlive", "false");  // Disable keep-alive
Connection conn = DriverManager.getConnection("jdbc:http://localhost:9999/", props);
```

### 3. Combined Configuration Example

```java
Properties props = new Properties();
props.setProperty("user", "myuser");
props.setProperty("password", "mypass");
props.setProperty("logLevel", "DEBUG");
props.setProperty("connectTimeout", "10000");
props.setProperty("readTimeout", "45000");
props.setProperty("keepAlive", "true");  // Enable keep-alive (default)

Connection conn = DriverManager.getConnection("jdbc:http://localhost:9999/", props);
```

## When to Disable Keep-Alive

Consider disabling keep-alive in these scenarios:
- **Firewall Issues**: Some firewalls drop idle connections
- **Load Balancers**: Some load balancers don't handle keep-alive well
- **Short-lived Applications**: Apps that make very few requests
- **Debugging**: To ensure each request uses a fresh connection

## System Property Reference

| Property | Default | Description |
|----------|---------|-------------|
| `jdbc.http.log.level` | INFO | Logging level (ERROR, WARN, INFO, DEBUG, TRACE) |
| `jdbc.http.connect.timeout` | 30000 | Connection timeout in milliseconds |
| `jdbc.http.read.timeout` | 60000 | Read timeout in milliseconds |
| `jdbc.http.keep.alive` | true | Enable HTTP keep-alive connections |

## Connection Property Reference

| Property | Default | Description |
|----------|---------|-------------|
| `logLevel` | INFO | Logging level for this connection |
| `connectTimeout` | 30000 | Connection timeout in milliseconds |
| `readTimeout` | 60000 | Read timeout in milliseconds |
| `keepAlive` | true | Enable HTTP keep-alive for this connection |