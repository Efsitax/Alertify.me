package com.alertify.worker.domain.exception;

import com.alertify.common.domain.exception.NotFoundException;

import java.util.UUID;

public class SnapshotNotFoundException extends NotFoundException {
    public SnapshotNotFoundException(UUID id) {
        super("SNAPSHOT", id.toString());
    }
}
