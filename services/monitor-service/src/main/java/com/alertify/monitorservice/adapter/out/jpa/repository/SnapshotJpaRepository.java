package com.alertify.monitorservice.adapter.out.jpa.repository;

import com.alertify.monitorservice.adapter.out.jpa.entity.SnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SnapshotJpaRepository extends JpaRepository<SnapshotJpaEntity, UUID> {
    Optional<SnapshotJpaEntity> findTop1ByMonitorIdOrderByAtDesc(UUID monitorId);
}
