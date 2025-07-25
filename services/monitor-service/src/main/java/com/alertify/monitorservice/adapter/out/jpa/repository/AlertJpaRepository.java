package com.alertify.monitorservice.adapter.out.jpa.repository;

import com.alertify.monitorservice.adapter.out.jpa.entity.AlertJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, UUID> {
}
