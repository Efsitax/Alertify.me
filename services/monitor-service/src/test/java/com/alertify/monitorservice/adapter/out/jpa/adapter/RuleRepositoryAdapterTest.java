package com.alertify.monitorservice.adapter.out.jpa.adapter;

import com.alertify.monitorservice.BaseIntegrationTest;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.entity.Rule;
import com.alertify.monitorservice.domain.repository.MonitorRepository;
import com.alertify.monitorservice.domain.repository.RuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuleRepositoryAdapterTest extends BaseIntegrationTest {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private MonitorRepository monitorRepository;

    private UUID createMonitorForTest() {
        Monitor monitor = Monitor.builder()
                .tenantId(UUID.randomUUID())
                .sourceType("ECOMMERCE_PRODUCT")
                .url("https://dummy.com/product/test")
                .params(Map.of("store", "DummyStore", "currency", "TRY"))
                .notifyPolicy(Map.of("channels", List.of("EMAIL"), "throttleMinutes", 60))
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        return monitorRepository.save(monitor).getId();
    }

    @Test
    void saveAndFindRule() {
        UUID monitorId = createMonitorForTest();

        Rule rule = Rule.builder()
                .type("PERCENT_DROP")
                .config(Map.of("percent", 15))
                .monitor(Monitor.builder().id(monitorId).build())
                .build();

        Rule saved = ruleRepository.save(rule);
        assertThat(saved.getId()).isNotNull();

        Optional<Rule> fetched = ruleRepository.findById(saved.getId().toString());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getType()).isEqualTo("PERCENT_DROP");
        assertThat(fetched.get().getMonitor().getId()).isEqualTo(monitorId);
    }

    @Test
    void findByMonitorId() {
        UUID monitorId = createMonitorForTest();

        Rule rule = Rule.builder()
                .type("TARGET_PRICE")
                .config(Map.of("targetPrice", 1000))
                .monitor(Monitor.builder().id(monitorId).build())
                .build();

        ruleRepository.save(rule);

        List<Rule> rules = ruleRepository.findByMonitorId(monitorId.toString());
        assertThat(rules).hasSize(1);
        assertThat(rules.getFirst().getMonitor().getId()).isEqualTo(monitorId);
    }

    @Test
    void deleteRule() {
        UUID monitorId = createMonitorForTest();

        Rule rule = Rule.builder()
                .type("TARGET_PRICE")
                .config(Map.of("targetPrice", 900))
                .monitor(Monitor.builder().id(monitorId).build())
                .build();

        Rule savedRule = ruleRepository.save(rule);
        String ruleId = savedRule.getId().toString();

        ruleRepository.delete(ruleId);
        assertThat(ruleRepository.findById(ruleId)).isEmpty();
    }
}