package com.alertify.monitorservice.adapter.out.jpa.adapter;

import com.alertify.monitorservice.adapter.out.jpa.entity.MonitorJpaEntity;
import com.alertify.monitorservice.adapter.out.jpa.entity.RuleJpaEntity;
import com.alertify.monitorservice.adapter.out.jpa.repository.MonitorJpaRepository;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.entity.Rule;
import com.alertify.monitorservice.domain.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Primary
@Component
@RequiredArgsConstructor
public class MonitorRepositoryAdapter implements MonitorRepository {

    private final MonitorJpaRepository repository;

    @Override
    public Monitor save(Monitor monitor) {
        MonitorJpaEntity entity;

        if (monitor.getId() != null) {
            entity = repository.findById(monitor.getId()).orElse(new MonitorJpaEntity());
        } else {
            entity = new MonitorJpaEntity();
        }

        entity.setTenantId(monitor.getTenantId());
        entity.setSourceType(monitor.getSourceType());
        entity.setUrl(monitor.getUrl());
        entity.setParams(monitor.getParams());
        entity.setNotifyPolicy(monitor.getNotifyPolicy());
        entity.setStatus(monitor.getStatus());
        entity.setCreatedAt(monitor.getCreatedAt());

        if (entity.getRules() == null) {
            entity.setRules(new ArrayList<>());
        } else {
            entity.getRules().clear();
        }
        if (monitor.getRules() != null) {
            entity.getRules().addAll(
                    monitor.getRules().stream()
                            .map(rule -> RuleJpaEntity.builder()
                                    .monitor(entity)
                                    .type(rule.getType())
                                    .config(rule.getConfig())
                                    .build()
                            ).toList()
            );
        }

        return mapToDomain(repository.save(entity));
    }

    @Override
    public List<Monitor> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Monitor> findById(String id) {
        return repository.findById(UUID.fromString(id))
                .map(this::mapToDomain);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(UUID.fromString(id));
    }

    @Override
    public Boolean existsById(String id) {
        return repository.existsById(UUID.fromString(id));
    }

    @Override
    public Boolean existsByUrl(String url) {
        return repository.existsByUrl(url);
    }

    @Override
    public List<Monitor> findByStatus(String status) {
        return repository.findByStatusWithRules(status).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Monitor mapToDomain(MonitorJpaEntity entity) {
        List<Rule> rules = entity.getRules() != null
                ? entity.getRules().stream()
                .map(rule -> Rule.builder()
                        .id(rule.getId())
                        .type(rule.getType())
                        .config(rule.getConfig())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        Monitor monitor = Monitor.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .sourceType(entity.getSourceType())
                .url(entity.getUrl())
                .params(entity.getParams())
                .rules(rules)
                .notifyPolicy(entity.getNotifyPolicy())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();

        rules.forEach(rule -> rule.setMonitor(monitor));

        return monitor;
    }
}
