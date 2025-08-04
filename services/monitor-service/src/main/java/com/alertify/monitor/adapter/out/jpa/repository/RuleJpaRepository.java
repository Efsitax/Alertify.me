package com.alertify.monitor.adapter.out.jpa.repository;

import com.alertify.monitor.adapter.out.jpa.entity.RuleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RuleJpaRepository extends JpaRepository<RuleJpaEntity, UUID> {
    List<RuleJpaEntity> findByMonitor_Id(UUID monitorId);
}
