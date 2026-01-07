package com.fall14123.jdbc.http;

public class HttpJdbcLogger {
    private LogLevel currentLevel;
    private final String componentName;

    public HttpJdbcLogger(String componentName) {
        this.componentName = componentName;
        this.currentLevel = LogLevel.fromString(System.getProperty("jdbc.http.log.level", "INFO"));
    }

    public HttpJdbcLogger(String componentName, LogLevel level) {
        this.componentName = componentName;
        this.currentLevel = level;
    }

    public void setLogLevel(LogLevel level) {
        this.currentLevel = level;
    }

    public LogLevel getLogLevel() {
        return currentLevel;
    }

    public boolean isEnabled(LogLevel level) {
        return currentLevel.isEnabled(level);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    private void log(LogLevel level, String message) {
        if (isEnabled(level)) {
            System.out.println(String.format("[%s] %s: %s", 
                level.name(), componentName, message));
        }
    }
}