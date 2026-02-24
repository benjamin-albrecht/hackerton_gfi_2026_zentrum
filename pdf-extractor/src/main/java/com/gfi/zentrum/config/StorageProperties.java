package com.gfi.zentrum.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zentrum.storage")
public class StorageProperties {

    private String path = "data/extractions";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
