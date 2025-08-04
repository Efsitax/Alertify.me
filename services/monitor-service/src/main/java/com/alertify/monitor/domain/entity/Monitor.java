package com.alertify.monitor.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Monitor {

    private UUID id;
    private UUID tenantId;
    private String sourceType;
    private String url;
    private Map<String, String> params;
    private List<Rule> rules;
    private Map<String,Object> notifyPolicy;
    private String status;
    private Instant createdAt;
}
