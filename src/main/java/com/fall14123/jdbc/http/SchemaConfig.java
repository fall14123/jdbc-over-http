package com.fall14123.jdbc.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SchemaConfig {
    // Request config
    public String requestContentType = "application/json";
    public String requestTemplate = "{\"sql\": \"${sql}\", \"parameters\": ${parameters}}";
    public String parameterTemplate = "{\"value\": ${value}, \"type\": \"${type}\"}";
    public String urlSuffix = "";  // e.g., "?default_format=JSONCompact"
    
    // Response config
    public boolean responseNdjson = true;
    public String columnsPath = "$._meta.columns[*]";
    public String columnNameField = "name";
    public String columnTypeField = "type";
    public String errorPath = "$.error";
    public String updateCountPath = "$.updateCount";
    public boolean rowsAsObjects = true;  // true = {"col": "val"}, false = ["val"]
    public String rowsPath = "$.data[*]"; // only used when rowsAsObjects=false and responseNdjson=false

    public static SchemaConfig load(String name) {
        SchemaConfig config = new SchemaConfig();
        String resourcePath = "/schemas/" + name + ".properties";
        try (InputStream is = SchemaConfig.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                // Try loading from classpath root
                try (InputStream is2 = SchemaConfig.class.getClassLoader().getResourceAsStream("schemas/" + name + ".properties")) {
                    if (is2 != null) {
                        config.loadFromProperties(is2);
                    }
                }
            } else {
                config.loadFromProperties(is);
            }
        } catch (IOException e) {
            // Return defaults
        }
        return config;
    }

    private void loadFromProperties(InputStream is) throws IOException {
        Properties props = new Properties();
        props.load(is);
        
        requestContentType = props.getProperty("request.contentType", requestContentType);
        requestTemplate = props.getProperty("request.template", requestTemplate);
        parameterTemplate = props.getProperty("request.parameterTemplate", parameterTemplate);
        urlSuffix = props.getProperty("request.urlSuffix", urlSuffix);
        
        responseNdjson = Boolean.parseBoolean(props.getProperty("response.ndjson", String.valueOf(responseNdjson)));
        columnsPath = props.getProperty("response.columnsPath", columnsPath);
        columnNameField = props.getProperty("response.columnNameField", columnNameField);
        columnTypeField = props.getProperty("response.columnTypeField", columnTypeField);
        errorPath = props.getProperty("response.errorPath", errorPath);
        updateCountPath = props.getProperty("response.updateCountPath", updateCountPath);
        rowsAsObjects = Boolean.parseBoolean(props.getProperty("response.rowsAsObjects", String.valueOf(rowsAsObjects)));
        rowsPath = props.getProperty("response.rowsPath", rowsPath);
    }
}
