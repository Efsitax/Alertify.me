package com.alertify.worker.adapter.out.jpa.repository;

import com.alertify.worker.adapter.out.jpa.entity.SnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SnapshotJpaRepository extends JpaRepository<SnapshotJpaEntity, UUID> {
    Optional<SnapshotJpaEntity> findTop1ByMonitorIdOrderByAtDesc(UUID monitorId);
}