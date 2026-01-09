package io.github.fall14123.jdbc.http;

public enum LogLevel {
    ERROR(0),
    WARN(1),
    INFO(2),
    DEBUG(3),
    TRACE(4);

    private final int level;

    LogLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isEnabled(LogLevel other) {
        return this.level >= other.level;
    }

    public static LogLevel fromString(String level) {
        if (level == null) {
            return INFO;
        }
        
        try {
            return LogLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return INFO;
        }
    }
}