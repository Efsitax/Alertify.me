package com.alertify.worker.domain.repository;

import com.alertify.worker.domain.entity.Alert;

public interface AlertRepository {
    Alert save(Alert alert);
}