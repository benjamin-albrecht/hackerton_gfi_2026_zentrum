package com.gfi.zentrum;

import com.gfi.zentrum.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ZentrumApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZentrumApplication.class, args);
    }
}
