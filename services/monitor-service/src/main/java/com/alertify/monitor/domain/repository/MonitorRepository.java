package com.alertify.monitor.domain.repository;

import com.alertify.monitor.domain.entity.Monitor;

import java.util.List;
import java.util.Optional;

public interface MonitorRepository {

    Monitor save (Monitor monitor);
    List<Monitor> findAll();
    Optional<Monitor> findById(String id);
    void delete(String id);
    Boolean existsById(String id);
    Boolean existsByUrl(String url);
    List<Monitor> findByStatus(String status);
}
