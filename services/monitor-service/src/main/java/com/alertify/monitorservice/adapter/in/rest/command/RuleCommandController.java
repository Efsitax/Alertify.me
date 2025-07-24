package com.alertify.monitorservice.adapter.in.rest.command;

import com.alertify.monitorservice.application.command.dto.rule.CreateRuleRequest;
import com.alertify.monitorservice.application.command.dto.rule.UpdateRuleRequest;
import com.alertify.monitorservice.application.command.handler.rule.CreateRuleHandler;
import com.alertify.monitorservice.application.command.handler.rule.DeleteRuleHandler;
import com.alertify.monitorservice.application.command.handler.rule.UpdateRuleHandler;
import com.alertify.monitorservice.application.mapper.RuleMapper;
import com.alertify.monitorservice.application.query.dto.rule.RuleResponse;
import com.alertify.monitorservice.domain.entity.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitors/rules")
@RequiredArgsConstructor
public class RuleCommandController {

    private final CreateRuleHandler createRuleHandler;
    private final UpdateRuleHandler updateRuleHandler;
    private final DeleteRuleHandler deleteRuleHandler;
    private final RuleMapper ruleMapper;

    @PostMapping
    public ResponseEntity<RuleResponse> createRule(
            @RequestBody CreateRuleRequest request
    ) {
        Rule created = createRuleHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ruleMapper.toResponse(created));
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<RuleResponse> updateRule(
            @PathVariable String ruleId,
            @RequestBody UpdateRuleRequest request
    ) {
        Rule updated = updateRuleHandler.handle(ruleId, request);
        return ResponseEntity.ok(ruleMapper.toResponse(updated));
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(
            @PathVariable String ruleId
    ) {
        deleteRuleHandler.handle(ruleId);
        return ResponseEntity.noContent().build();
    }
}