package com.alertify.monitor.adapter.out.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;

@Slf4j
@Component("application")
public class ApplicationHealthIndicator implements HealthIndicator {

    @Value("${spring.application.name:monitor-service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public Health health() {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

            long uptimeMillis = runtimeBean.getUptime();
            long uptimeSeconds = uptimeMillis / 1000;

            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

            Health.Builder healthBuilder = memoryUsagePercent > 90 ? Health.down() : Health.up();

            return healthBuilder
                    .withDetail("application", applicationName)
                    .withDetail("port", serverPort)
                    .withDetail("uptimeSeconds", uptimeSeconds)
                    .withDetail("javaVersion", System.getProperty("java.version"))
                    .withDetail("memoryUsagePercent", String.format("%.2f", memoryUsagePercent))
                    .withDetail("usedMemoryMB", usedMemory / 1024 / 1024)
                    .withDetail("maxMemoryMB", maxMemory / 1024 / 1024)
                    .build();

        } catch (Exception e) {
            log.error("Application health check failed", e);
            return Health.down()
                    .withDetail("application", applicationName)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}