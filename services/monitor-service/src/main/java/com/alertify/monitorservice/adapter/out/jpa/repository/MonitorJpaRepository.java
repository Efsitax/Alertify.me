package com.alertify.monitorservice.adapter.out.jpa.repository;

import com.alertify.monitorservice.adapter.out.jpa.entity.MonitorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitorJpaRepository extends JpaRepository<MonitorJpaEntity, UUID> {
    Boolean existsByUrl(String url);
    @Query("SELECT m FROM MonitorJpaEntity m LEFT JOIN FETCH m.rules WHERE m.status = :status")
    List<MonitorJpaEntity> findByStatusWithRules(@Param("status") String status);
}
