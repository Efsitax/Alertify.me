package com.alertify.monitorservice.application.mapper;

import com.alertify.monitorservice.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitorservice.domain.entity.Monitor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = RuleMapper.class)
public interface MonitorMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "tenantId", target = "tenantId")
    @Mapping(source = "sourceType", target = "sourceType")
    @Mapping(source = "url", target = "url")
    @Mapping(source = "params", target = "params")
    @Mapping(source = "rules", target = "rules")
    @Mapping(source = "notifyPolicy", target = "notifyPolicy")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    MonitorResponse toDto(Monitor monitor);
}