package com.alertify.monitorservice;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.alertify.monitorservice.adapter.out.jpa.repository")
@EntityScan(basePackages = "com.alertify.monitorservice.adapter.out.jpa.entity")
@ComponentScan(basePackages = {
        "com.alertify.monitorservice.adapter.out.jpa.adapter",
        "com.alertify.monitorservice.application.mapper",
        "com.alertify.monitorservice.scheduler"
})
public class AdapterConfig {
}