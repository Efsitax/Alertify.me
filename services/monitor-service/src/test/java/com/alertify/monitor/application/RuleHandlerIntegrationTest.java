package com.alertify.monitor.application;

import com.alertify.monitor.BaseIntegrationTest;
import com.alertify.monitor.application.command.dto.rule.CreateRuleRequest;
import com.alertify.monitor.application.command.dto.rule.UpdateRuleRequest;
import com.alertify.monitor.application.command.handler.rule.CreateRuleHandler;
import com.alertify.monitor.application.command.handler.rule.DeleteRuleHandler;
import com.alertify.monitor.application.command.handler.rule.UpdateRuleHandler;
import com.alertify.monitor.application.query.handler.rule.GetRulesByMonitorIdHandler;
import com.alertify.monitor.domain.entity.Monitor;
import com.alertify.monitor.domain.entity.Rule;
import com.alertify.monitor.domain.exception.MonitorNotFoundException;
import com.alertify.monitor.domain.exception.RuleNotFoundException;
import com.alertify.monitor.domain.repository.MonitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleHandlerIntegrationTest extends BaseIntegrationTest {

    @Autowired private CreateRuleHandler createRuleHandler;
    @Autowired private UpdateRuleHandler updateRuleHandler;
    @Autowired private DeleteRuleHandler deleteRuleHandler;
    @Autowired private GetRulesByMonitorIdHandler getRulesHandler;

    @Autowired private MonitorRepository monitorRepository;

    private UUID monitorId;

    @BeforeEach
    void initMonitor() {
        Monitor monitor = Monitor.builder()
                .tenantId(UUID.randomUUID())
                .sourceType("ECOMMERCE_PRODUCT")
                .url("https://dummy.com/monitor-test")
                .params(Map.of("store", "TestStore", "currency", "TRY"))
                .notifyPolicy(Map.of("channels", List.of("EMAIL"), "throttleMinutes", 60))
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        monitorId = monitorRepository.save(monitor).getId();
    }

    @Test
    void createRuleAndValidateErrors() {
        Rule created = createRuleHandler.handle(new CreateRuleRequest(
                monitorId.toString(),
                "TARGET_PRICE",
                Map.of("targetPrice", 1200)
        ));
        assertThat(created.getId()).isNotNull();

        assertThatThrownBy(() -> createRuleHandler.handle(new CreateRuleRequest(
                UUID.randomUUID().toString(), "PERCENT_DROP", Map.of("percent", 10)
        ))).isInstanceOf(MonitorNotFoundException.class);

        assertThatThrownBy(() -> createRuleHandler.handle(new CreateRuleRequest(
                monitorId.toString(), "", Map.of("percent", 5)
        ))).hasMessageContaining("Rule type cannot be empty");

        assertThatThrownBy(() -> createRuleHandler.handle(new CreateRuleRequest(
                monitorId.toString(), "TARGET_PRICE", Map.of()
        ))).hasMessageContaining("Rule config cannot be empty");
    }

    @Test
    void updateRuleAndHandleValidation() {
        Rule created = createRuleHandler.handle(new CreateRuleRequest(
                monitorId.toString(),
                "PERCENT_DROP",
                Map.of("percent", 10)
        ));

        Rule updated = updateRuleHandler.handle(created.getId().toString(),
                new UpdateRuleRequest("TARGET_PRICE", Map.of("targetPrice", 999)));
        assertThat(updated.getType()).isEqualTo("TARGET_PRICE");

        assertThatThrownBy(() -> updateRuleHandler.handle(UUID.randomUUID().toString(),
                new UpdateRuleRequest("TARGET_PRICE", Map.of("targetPrice", 500))))
                .isInstanceOf(RuleNotFoundException.class);

        assertThatThrownBy(() -> updateRuleHandler.handle(created.getId().toString(),
                new UpdateRuleRequest("", Map.of("targetPrice", 800))))
                .hasMessageContaining("Rule type cannot be empty");
        assertThatThrownBy(() -> updateRuleHandler.handle(created.getId().toString(),
                new UpdateRuleRequest("TARGET_PRICE", Map.of())))
                .hasMessageContaining("Rule config cannot be empty");
    }

    @Test
    void deleteRuleAndEnsureNotFound() {
        Rule created = createRuleHandler.handle(new CreateRuleRequest(
                monitorId.toString(),
                "PERCENT_DROP",
                Map.of("percent", 5)
        ));

        deleteRuleHandler.handle(created.getId().toString());
        assertThatThrownBy(() -> deleteRuleHandler.handle(created.getId().toString()))
                .isInstanceOf(RuleNotFoundException.class);
    }

    @Test
    void getRulesByMonitorIdOrThrow() {
        createRuleHandler.handle(new CreateRuleRequest(
                monitorId.toString(),
                "PERCENT_DROP",
                Map.of("percent", 15)
        ));

        var rules = getRulesHandler.handle(monitorId.toString());
        assertThat(rules).isNotEmpty();
        assertThat(rules.getFirst().type()).isEqualTo("PERCENT_DROP");

        assertThatThrownBy(() -> getRulesHandler.handle(UUID.randomUUID().toString()))
                .isInstanceOf(RuleNotFoundException.class);
    }
}