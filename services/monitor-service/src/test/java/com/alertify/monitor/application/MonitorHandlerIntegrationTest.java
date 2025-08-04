package com.alertify.monitor.application;

import com.alertify.monitor.BaseIntegrationTest;
import com.alertify.monitor.application.command.dto.monitor.CreateMonitorRequest;
import com.alertify.monitor.application.command.dto.monitor.UpdateMonitorRequest;
import com.alertify.monitor.application.command.handler.monitor.CreateMonitorHandler;
import com.alertify.monitor.application.command.handler.monitor.DeleteMonitorHandler;
import com.alertify.monitor.application.command.handler.monitor.UpdateMonitorHandler;
import com.alertify.monitor.application.query.handler.monitor.GetMonitorByIdHandler;
import com.alertify.monitor.application.query.handler.monitor.ListMonitorsHandler;
import com.alertify.monitor.domain.exception.MonitorNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonitorHandlerIntegrationTest extends BaseIntegrationTest {

    @Autowired private CreateMonitorHandler createHandler;
    @Autowired private UpdateMonitorHandler updateHandler;
    @Autowired private DeleteMonitorHandler deleteHandler;
    @Autowired private ListMonitorsHandler listHandler;
    @Autowired private GetMonitorByIdHandler getHandler;

    private CreateMonitorRequest sampleRequest() {
        return new CreateMonitorRequest(
                UUID.randomUUID().toString(),
                "ECOMMERCE_PRODUCT",
                "https://dummy.com/product",
                Map.of("currency", "TRY", "store", "TestStore"),
                List.of(),
                Map.of("channels", List.of("EMAIL"), "throttleMinutes", 60)
        );
    }

    @Test
    void createMonitorAndPreventDuplicateUrl() {
        var created = createHandler.handle(sampleRequest());
        assertThat(created.getId()).isNotNull();

        assertThatThrownBy(() -> createHandler.handle(sampleRequest()))
                .hasMessageContaining("Monitor for this URL already exists");
    }

    @Test
    void updateMonitorOrThrowIfNotFound() {
        var created = createHandler.handle(sampleRequest());

        UpdateMonitorRequest updateReq = new UpdateMonitorRequest(
                "ECOMMERCE_PRODUCT",
                "https://dummy.com/product",
                Map.of("currency", "USD"),
                List.of(),
                Map.of("channels", List.of("EMAIL")),
                "ACTIVE"
        );

        var updated = updateHandler.handle(created.getId().toString(), updateReq);
        assertThat(updated.getParams()).containsEntry("currency", "USD");

        assertThatThrownBy(() -> updateHandler.handle(UUID.randomUUID().toString(), updateReq))
                .isInstanceOf(MonitorNotFoundException.class);
    }

    @Test
    void deleteMonitorOrThrowIfNotFound() {
        var created = createHandler.handle(sampleRequest());

        deleteHandler.handle(created.getId().toString());
        assertThatThrownBy(() -> deleteHandler.handle(created.getId().toString()))
                .isInstanceOf(MonitorNotFoundException.class);
    }

    @Test
    void listAndGetMonitors() {
        var created = createHandler.handle(sampleRequest());

        var list = listHandler.handle(null);
        assertThat(list).isNotEmpty();

        var fetched = getHandler.handle(created.getId().toString());
        assertThat(fetched.id()).isEqualTo(created.getId().toString());

        UpdateMonitorRequest updateReq = new UpdateMonitorRequest(
                created.getSourceType(),
                created.getUrl(),
                created.getParams(),
                created.getRules(),
                created.getNotifyPolicy(),
                "PASSIVE"
        );

        var updated = updateHandler.handle(created.getId().toString(), updateReq);
        assertThat(updated.getStatus()).isEqualTo("PASSIVE");
    }
}