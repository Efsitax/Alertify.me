package com.alertify.monitor;

import com.alertify.monitor.adapter.out.metrics.MonitorMetricsService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CommonTestConfig {

    @MockBean
    private MonitorMetricsService monitorMetricsService;

    // --- Monitor Handlers ---
    @Bean
    CreateMonitorHandler createMonitorHandler(MonitorRepository repo,
                                              MonitorMetricsService metricsService) {
        return new CreateMonitorHandler(repo, metricsService);
    }

    @Bean
    UpdateMonitorHandler updateMonitorHandler(MonitorRepository repo,
                                              MonitorMetricsService metricsService) {
        return new UpdateMonitorHandler(repo, metricsService);
    }

    @Bean
    DeleteMonitorHandler deleteMonitorHandler(MonitorRepository repo,
                                              MonitorMetricsService metricsService) {
        return new DeleteMonitorHandler(repo, metricsService);
    }

    @Bean
    ListMonitorsHandler listMonitorsHandler(MonitorRepository repo,
                                            MonitorMapper mapper) {
        return new ListMonitorsHandler(repo, mapper);
    }

    @Bean
    GetMonitorByIdHandler getMonitorByIdHandler(MonitorRepository repo,
                                                MonitorMapper mapper) {
        return new GetMonitorByIdHandler(repo, mapper);
    }

    // --- Rule Handlers ---
    @Bean
    CreateRuleHandler createRuleHandler(MonitorRepository monitorRepo,
                                        RuleRepository ruleRepo,
                                        MonitorMetricsService metricsService) {
        return new CreateRuleHandler(monitorRepo, ruleRepo, metricsService);
    }

    @Bean
    UpdateRuleHandler updateRuleHandler(RuleRepository ruleRepo,
                                        MonitorMetricsService metricsService) {
        return new UpdateRuleHandler(ruleRepo, metricsService);
    }

    @Bean
    DeleteRuleHandler deleteRuleHandler(RuleRepository ruleRepo,
                                        MonitorMetricsService metricsService) {
        return new DeleteRuleHandler(ruleRepo, metricsService);
    }

    @Bean
    GetRulesByMonitorIdHandler getRulesByMonitorIdHandler(RuleRepository ruleRepo, RuleMapper mapper) {
        return new GetRulesByMonitorIdHandler(ruleRepo, mapper);
    }
}