package com.alertify.monitorservice.adapter.out.jpa.adapter;

import com.alertify.monitorservice.adapter.out.jpa.entity.SnapshotJpaEntity;
import com.alertify.monitorservice.adapter.out.jpa.repository.SnapshotJpaRepository;
import com.alertify.monitorservice.domain.entity.Snapshot;
import com.alertify.monitorservice.domain.repository.SnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Primary
@Component
@RequiredArgsConstructor
public class SnapshotRepositoryAdapter implements SnapshotRepository {

    private final SnapshotJpaRepository repository;

    @Override
    public Snapshot save(Snapshot snapshot) {
        SnapshotJpaEntity entity = new SnapshotJpaEntity();
        entity.setId(snapshot.getId() != null ? snapshot.getId() : UUID.randomUUID());
        entity.setMonitorId(snapshot.getMonitorId());
        entity.setMetric(snapshot.getMetric());
        entity.setValue(snapshot.getValue());
        entity.setUnit(snapshot.getUnit());
        entity.setAt(snapshot.getAt());

        SnapshotJpaEntity saved = repository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Snapshot> findLastByMonitorId(UUID monitorId) {
        return repository.findTop1ByMonitorIdOrderByAtDesc(monitorId)
                .map(this::mapToDomain);
    }

    private Snapshot mapToDomain(SnapshotJpaEntity entity) {
        return Snapshot.builder()
                .id(entity.getId())
                .monitorId(entity.getMonitorId())
                .metric(entity.getMetric())
                .value(entity.getValue())
                .unit(entity.getUnit())
                .at(entity.getAt())
                .build();
    }
}
