package com.alertify.monitor;

import com.alertify.monitor.application.command.handler.monitor.*;
import com.alertify.monitor.application.command.handler.rule.CreateRuleHandler;
import com.alertify.monitor.application.command.handler.rule.DeleteRuleHandler;
import com.alertify.monitor.application.command.handler.rule.UpdateRuleHandler;
import com.alertify.monitor.application.query.handler.monitor.*;
import com.alertify.monitor.application.query.handler.rule.GetRulesByMonitorIdHandler;
import com.alertify.monitor.application.mapper.MonitorMapper;
import com.alertify.monitor.application.mapper.RuleMapper;
import com.alertify.monitor.domain.repository.MonitorRepository;
import com.alertify.monitor.domain.repository.RuleRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CommonTestConfig {

    // --- Monitor Handlers ---
    @Bean
    CreateMonitorHandler createMonitorHandler(MonitorRepository repo) {
        return new CreateMonitorHandler(repo);
    }

    @Bean
    UpdateMonitorHandler updateMonitorHandler(MonitorRepository repo) {
        return new UpdateMonitorHandler(repo);
    }

    @Bean
    DeleteMonitorHandler deleteMonitorHandler(MonitorRepository repo) {
        return new DeleteMonitorHandler(repo);
    }

    @Bean
    ListMonitorsHandler listMonitorsHandler(MonitorRepository repo, MonitorMapper mapper) {
        return new ListMonitorsHandler(repo, mapper);
    }

    @Bean
    GetMonitorByIdHandler getMonitorByIdHandler(MonitorRepository repo, MonitorMapper mapper) {
        return new GetMonitorByIdHandler(repo, mapper);
    }

    // --- Rule Handlers ---
    @Bean
    CreateRuleHandler createRuleHandler(MonitorRepository monitorRepo, RuleRepository ruleRepo) {
        return new CreateRuleHandler(monitorRepo, ruleRepo);
    }

    @Bean
    UpdateRuleHandler updateRuleHandler(RuleRepository ruleRepo) {
        return new UpdateRuleHandler(ruleRepo);
    }

    @Bean
    DeleteRuleHandler deleteRuleHandler(RuleRepository ruleRepo) {
        return new DeleteRuleHandler(ruleRepo);
    }

    @Bean
    GetRulesByMonitorIdHandler getRulesByMonitorIdHandler(RuleRepository ruleRepo, RuleMapper mapper) {
        return new GetRulesByMonitorIdHandler(ruleRepo, mapper);
    }
}