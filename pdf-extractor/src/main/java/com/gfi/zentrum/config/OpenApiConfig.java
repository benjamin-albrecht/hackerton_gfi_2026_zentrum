package com.gfi.zentrum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI zentrumOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zentrum PDF Extractor API")
                        .description("REST API for extracting IHK vocational training exam data from PDFs")
                        .version("1.0"));
    }
}
