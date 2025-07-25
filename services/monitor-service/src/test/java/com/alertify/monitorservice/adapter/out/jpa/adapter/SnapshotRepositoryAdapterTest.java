package com.alertify.monitorservice.adapter.out.jpa.adapter;

import com.alertify.monitorservice.BaseIntegrationTest;
import com.alertify.monitorservice.domain.entity.Snapshot;
import com.alertify.monitorservice.domain.repository.SnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotRepositoryAdapterTest extends BaseIntegrationTest {

    @Autowired
    private SnapshotRepository snapshotRepository;

    @Test
    void saveAndFindLastSnapshot() {
        UUID monitorId = UUID.randomUUID();

        Snapshot snap1 = Snapshot.builder()
                .monitorId(monitorId)
                .metric("price")
                .value(BigDecimal.valueOf(1500))
                .unit("TRY")
                .at(Instant.now().minusSeconds(120))
                .build();

        Snapshot snap2 = Snapshot.builder()
                .monitorId(monitorId)
                .metric("price")
                .value(BigDecimal.valueOf(1400))
                .unit("TRY")
                .at(Instant.now())
                .build();

        snapshotRepository.save(snap1);
        snapshotRepository.save(snap2);

        Optional<Snapshot> last = snapshotRepository.findLastByMonitorId(monitorId);
        assertThat(last).isPresent();
        assertThat(last.get().getValue()).isEqualByComparingTo(BigDecimal.valueOf(1400));
        assertThat(last.get().getMetric()).isEqualTo("price");
    }
}