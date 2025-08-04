package com.alertify.monitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI monitorServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Alertify Monitor Service API")
                        .description("Monitor and Rule management service for Alertify platform. " +
                                "This service provides CRUD operations for monitors (data source configurations) " +
                                "and rules (alert conditions).")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Kadir Uğurlu")
                                .email("kadir--ugurlu@hotmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ));
    }
}