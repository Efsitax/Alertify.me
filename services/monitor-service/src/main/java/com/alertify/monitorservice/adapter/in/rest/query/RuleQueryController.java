package com.alertify.monitorservice.adapter.in.rest.query;

import com.alertify.monitorservice.application.query.dto.rule.RuleResponse;
import com.alertify.monitorservice.application.query.handler.rule.GetRulesByMonitorIdHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitors/rules")
@RequiredArgsConstructor
public class RuleQueryController {

    private final GetRulesByMonitorIdHandler getRulesByMonitorIdHandler;

    @GetMapping("/{monitorId}")
    private ResponseEntity<List<RuleResponse>> getRule(@PathVariable("monitorId") String monitorId) {
        return ResponseEntity.ok(getRulesByMonitorIdHandler.handle(monitorId));
    }
}
