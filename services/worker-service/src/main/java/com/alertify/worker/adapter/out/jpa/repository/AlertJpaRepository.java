package com.alertify.worker.adapter.out.jpa.repository;

import com.alertify.worker.adapter.out.jpa.entity.AlertJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, UUID> {
}
