package com.alertify.monitor;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.alertify.monitor.adapter.out.jpa.repository")
@EntityScan(basePackages = "com.alertify.monitor.adapter.out.jpa.entity")
@ComponentScan(basePackages = {
        "com.alertify.monitor.adapter.out.jpa.adapter",
        "com.alertify.monitor.application.mapper",
        "com.alertify.monitor.scheduler"
})
public class AdapterConfig {
}