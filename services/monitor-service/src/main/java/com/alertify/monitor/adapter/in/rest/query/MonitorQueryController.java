package com.alertify.monitor.adapter.in.rest.query;

import com.alertify.monitor.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitor.application.query.handler.monitor.GetMonitorByIdHandler;
import com.alertify.monitor.application.query.handler.monitor.ListMonitorsHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
public class MonitorQueryController {

    private final ListMonitorsHandler listMonitorsHandler;
    private final GetMonitorByIdHandler getMonitorByIdHandler;

    @GetMapping
    public ResponseEntity<List<MonitorResponse>> listMonitors(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(listMonitorsHandler.handle(status));
    }

    @GetMapping("/{monitorId}")
    public ResponseEntity<MonitorResponse> getMonitor(@PathVariable String monitorId) {
        return ResponseEntity.ok(getMonitorByIdHandler.handle(monitorId));
    }
}