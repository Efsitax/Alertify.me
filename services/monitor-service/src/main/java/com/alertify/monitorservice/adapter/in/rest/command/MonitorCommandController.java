package com.alertify.monitorservice.adapter.in.rest.command;

import com.alertify.monitorservice.application.command.dto.monitor.UpdateMonitorRequest;
import com.alertify.monitorservice.application.command.handler.monitor.CreateMonitorHandler;
import com.alertify.monitorservice.application.command.dto.monitor.CreateMonitorRequest;
import com.alertify.monitorservice.application.command.handler.monitor.DeleteMonitorHandler;
import com.alertify.monitorservice.application.command.handler.monitor.UpdateMonitorHandler;
import com.alertify.monitorservice.application.mapper.MonitorMapper;
import com.alertify.monitorservice.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitorservice.domain.entity.Monitor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
public class MonitorCommandController {

    private final CreateMonitorHandler createMonitorHandler;
    private final UpdateMonitorHandler updateMonitorHandler;
    private final DeleteMonitorHandler deleteMonitorHandler;
    private final MonitorMapper monitorMapper;

    @PostMapping
    public ResponseEntity<MonitorResponse> createMonitor(@RequestBody CreateMonitorRequest request) {
        Monitor monitor = createMonitorHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(monitorMapper.toDto(monitor));
    }

    @PutMapping("/{monitorId}")
    public ResponseEntity<MonitorResponse> updateMonitor(
            @PathVariable String monitorId,
            @RequestBody UpdateMonitorRequest request) {
        Monitor monitor = updateMonitorHandler.handle(monitorId, request);
        return ResponseEntity.ok(monitorMapper.toDto(monitor));
    }

    @DeleteMapping("/{monitorId}")
    public ResponseEntity<Void> deleteMonitor(@PathVariable String monitorId) {
        deleteMonitorHandler.handle(monitorId);
        return ResponseEntity.noContent().build();
    }
}
