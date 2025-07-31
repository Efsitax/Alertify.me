package com.alertify.monitor.domain.repository;

import com.alertify.monitor.domain.entity.Rule;

import java.util.List;
import java.util.Optional;

public interface RuleRepository {
    Rule save(Rule rule);
    Optional<Rule> findById(String id);
    void delete(String id);
    List<Rule> findByMonitorId(String monitorId);
}
