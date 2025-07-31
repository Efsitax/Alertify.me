package com.alertify.monitor.application.mapper;

import com.alertify.monitor.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitor.application.query.dto.rule.RuleResponse;
import com.alertify.monitor.domain.entity.Rule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RuleMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "config", target = "config")
    MonitorResponse.RuleResponse toDto(Rule rule);

    @Mapping(target = "id", expression = "java(rule.getId() != null ? rule.getId().toString() : null)")
    @Mapping(target = "monitorId", expression = "java(rule.getMonitor() != null ? rule.getMonitor().getId().toString() : null)")
    RuleResponse toResponse(Rule rule);
}
