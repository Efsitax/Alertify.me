package com.alertify.monitorservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {

    private UUID id;
    private String type;
    private Monitor monitor;
    private Map<String, Object> config;
}
