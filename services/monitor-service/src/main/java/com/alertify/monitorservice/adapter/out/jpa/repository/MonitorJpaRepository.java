package com.alertify.monitorservice.adapter.out.jpa.repository;

import com.alertify.monitorservice.adapter.out.jpa.entity.MonitorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MonitorJpaRepository extends JpaRepository<MonitorJpaEntity, UUID> {
    Boolean existsByUrl(String url);
}
