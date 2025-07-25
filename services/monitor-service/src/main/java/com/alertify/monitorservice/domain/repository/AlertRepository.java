package com.alertify.monitorservice.domain.repository;

import com.alertify.monitorservice.domain.entity.Alert;

public interface AlertRepository {
    Alert save (Alert alert);
}
