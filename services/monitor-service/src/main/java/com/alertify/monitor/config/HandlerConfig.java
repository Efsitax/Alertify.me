package com.alertify.monitor.config;

import com.alertify.monitor.adapter.out.jpa.adapter.MonitorRepositoryAdapter;
import com.alertify.monitor.adapter.out.jpa.repository.MonitorJpaRepository;
import com.alertify.monitor.adapter.out.metrics.MonitorMetricsService;
import com.alertify.monitor.application.command.handler.monitor.CreateMonitorHandler;
import com.alertify.monitor.application.command.handler.monitor.DeleteMonitorHandler;
import com.alertify.monitor.application.command.handler.monitor.UpdateMonitorHandler;
import com.alertify.monitor.application.command.handler.rule.CreateRuleHandler;
import com.alertify.monitor.application.command.handler.rule.DeleteRuleHandler;
import com.alertify.monitor.application.command.handler.rule.UpdateRuleHandler;
import com.alertify.monitor.application.mapper.MonitorMapper;
import com.alertify.monitor.application.mapper.RuleMapper;
import com.alertify.monitor.application.query.handler.monitor.GetMonitorByIdHandler;
import com.alertify.monitor.application.query.handler.monitor.ListMonitorsHandler;
import com.alertify.monitor.application.query.handler.rule.GetRulesByMonitorIdHandler;
import com.alertify.monitor.domain.repository.MonitorRepository;
import com.alertify.monitor.domain.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class HandlerConfig {
    // MONITOR HANDLERS
    @Bean
    public CreateMonitorHandler createMonitorHandler(MonitorRepository repository,
                                                     MonitorMetricsService metricsService) {
        return new CreateMonitorHandler(repository, metricsService);
    }

    @Bean
    public ListMonitorsHandler listMonitorsHandler(MonitorRepository repository,
                                                   @Qualifier("monitorMapperImpl") MonitorMapper mapper) {
        return new ListMonitorsHandler(repository, mapper);
    }

    @Bean
    public UpdateMonitorHandler updateMonitorHandler(MonitorRepository repository,
                                                     MonitorMetricsService metricsService) {
        return new UpdateMonitorHandler(repository, metricsService);
    }

    @Bean
    public DeleteMonitorHandler deleteMonitorHandler(MonitorRepository repository,
                                                     MonitorMetricsService metricsService) {
        return new DeleteMonitorHandler(repository, metricsService);
    }

    @Bean
    public GetMonitorByIdHandler getMonitorByIdHandler(MonitorRepository repository,
                                                       @Qualifier("monitorMapperImpl") MonitorMapper mapper) {
        return new GetMonitorByIdHandler(repository, mapper);
    }

    //RULE HANDLERS
    @Bean
    public CreateRuleHandler createRuleHandler(MonitorRepository monitorRepository,
                                               RuleRepository ruleRepository,
                                               MonitorMetricsService metricsService) {
        return new CreateRuleHandler(monitorRepository, ruleRepository, metricsService);
    }

    @Bean
    public UpdateRuleHandler updateRuleHandler(RuleRepository ruleRepository,
                                               MonitorMetricsService metricsService) {
        return new UpdateRuleHandler(ruleRepository, metricsService);
    }

    @Bean
    public DeleteRuleHandler deleteRuleHandler(RuleRepository ruleRepository,
                                               MonitorMetricsService metricsService) {
        return new DeleteRuleHandler(ruleRepository, metricsService);
    }

    @Bean
    public GetRulesByMonitorIdHandler getRulesByMonitorIdHandler(RuleRepository ruleRepository,
                                                                @Qualifier("ruleMapperImpl") RuleMapper mapper) {
        return new GetRulesByMonitorIdHandler(ruleRepository, mapper);
    }

    @Bean
    MonitorRepository monitorRepository(MonitorJpaRepository repo) {
        return new MonitorRepositoryAdapter(repo);
    }
}
