package com.alertify.monitorservice.adapter.out.jpa.adapter;

import com.alertify.monitorservice.adapter.out.jpa.entity.MonitorJpaEntity;
import com.alertify.monitorservice.adapter.out.jpa.entity.RuleJpaEntity;
import com.alertify.monitorservice.adapter.out.jpa.repository.RuleJpaRepository;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.entity.Rule;
import com.alertify.monitorservice.domain.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Component
@RequiredArgsConstructor
public class RuleRepositoryAdapter implements RuleRepository {

    private final RuleJpaRepository repository;

    @Override
    public Rule save(Rule rule) {
        RuleJpaEntity entity = new RuleJpaEntity();
        entity.setId(rule.getId() != null ? rule.getId() : UUID.randomUUID());
        entity.setType(rule.getType());
        entity.setConfig(rule.getConfig());

        MonitorJpaEntity monitorRef = new MonitorJpaEntity();
        monitorRef.setId(rule.getMonitor().getId());
        entity.setMonitor(monitorRef);

        RuleJpaEntity savedEntity = repository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public Optional<Rule> findById(String id) {
        return repository.findById(UUID.fromString(id))
                .map(this::mapToDomain);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(UUID.fromString(id));
    }

    @Override
    public List<Rule> findByMonitorId(String monitorId) {
        return repository.findByMonitor_Id(UUID.fromString(monitorId))
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    private Rule mapToDomain(RuleJpaEntity entity) {
        return Rule.builder()
                .id(entity.getId())
                .type(entity.getType())
                .config(entity.getConfig())
                .monitor(Monitor.builder()
                        .id(entity.getMonitor().getId())
                        .tenantId(entity.getMonitor().getTenantId())
                        .sourceType(entity.getMonitor().getSourceType())
                        .url(entity.getMonitor().getUrl())
                        .build())
                .build();
    }
}
