package com.alertify.monitorservice.config;

import com.alertify.monitorservice.adapter.out.jpa.adapter.MonitorRepositoryAdapter;
import com.alertify.monitorservice.adapter.out.jpa.repository.MonitorJpaRepository;
import com.alertify.monitorservice.application.command.handler.monitor.CreateMonitorHandler;
import com.alertify.monitorservice.application.command.handler.monitor.DeleteMonitorHandler;
import com.alertify.monitorservice.application.command.handler.monitor.UpdateMonitorHandler;
import com.alertify.monitorservice.application.command.handler.rule.CreateRuleHandler;
import com.alertify.monitorservice.application.command.handler.rule.DeleteRuleHandler;
import com.alertify.monitorservice.application.command.handler.rule.UpdateRuleHandler;
import com.alertify.monitorservice.application.mapper.MonitorMapper;
import com.alertify.monitorservice.application.mapper.RuleMapper;
import com.alertify.monitorservice.application.query.handler.monitor.GetMonitorByIdHandler;
import com.alertify.monitorservice.application.query.handler.monitor.ListMonitorsHandler;
import com.alertify.monitorservice.application.query.handler.rule.GetRulesByMonitorIdHandler;
import com.alertify.monitorservice.domain.repository.MonitorRepository;
import com.alertify.monitorservice.domain.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class HandlerConfig {
    // MONITOR HANDLERS
    @Bean
    public CreateMonitorHandler createMonitorHandler(MonitorRepository repository) {
        return new CreateMonitorHandler(repository);
    }

    @Bean
    public ListMonitorsHandler listMonitorsHandler(MonitorRepository repository,
                                                   @Qualifier("monitorMapperImpl") MonitorMapper mapper) {
        return new ListMonitorsHandler(repository, mapper);
    }

    @Bean
    public UpdateMonitorHandler updateMonitorHandler(MonitorRepository repository) {
        return new UpdateMonitorHandler(repository);
    }

    @Bean
    public DeleteMonitorHandler deleteMonitorHandler(MonitorRepository repository) {
        return new DeleteMonitorHandler(repository);
    }

    @Bean
    public GetMonitorByIdHandler getMonitorByIdHandler(MonitorRepository repository,
                                                       @Qualifier("monitorMapperImpl") MonitorMapper mapper) {
        return new GetMonitorByIdHandler(repository, mapper);
    }

    //RULE HANDLERS
    @Bean
    public CreateRuleHandler createRuleHandler(MonitorRepository monitorRepository, RuleRepository ruleRepository) {
        return new CreateRuleHandler(monitorRepository, ruleRepository);
    }

    @Bean
    public UpdateRuleHandler updateRuleHandler(RuleRepository ruleRepository) {
        return new UpdateRuleHandler(ruleRepository);
    }

    @Bean
    public DeleteRuleHandler deleteRuleHandler(RuleRepository ruleRepository) {
        return new DeleteRuleHandler(ruleRepository);
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
