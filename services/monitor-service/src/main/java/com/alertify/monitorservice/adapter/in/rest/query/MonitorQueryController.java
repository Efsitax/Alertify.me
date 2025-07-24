package com.alertify.monitorservice.adapter.in.rest.query;

import com.alertify.monitorservice.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitorservice.application.query.handler.monitor.GetMonitorByIdHandler;
import com.alertify.monitorservice.application.query.handler.monitor.ListMonitorsHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
public class MonitorQueryController {

    private final ListMonitorsHandler listMonitorsHandler;
    private final GetMonitorByIdHandler getMonitorByIdHandler;

    @GetMapping
    private ResponseEntity<List<MonitorResponse>> listMonitors() {
        return ResponseEntity.ok(listMonitorsHandler.handle());
    }

    @GetMapping("/{monitorId}")
    private ResponseEntity<MonitorResponse> getMonitor(@PathVariable String monitorId) {
        return ResponseEntity.ok(getMonitorByIdHandler.handle(monitorId));
    }
}
