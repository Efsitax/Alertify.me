package com.alertify.worker.domain.repository;

import com.alertify.worker.domain.entity.Snapshot;

import java.util.Optional;
import java.util.UUID;

public interface SnapshotRepository {
    Snapshot save(Snapshot snapshot);
    Optional<Snapshot> findLastByMonitorId(UUID monitorId);
}