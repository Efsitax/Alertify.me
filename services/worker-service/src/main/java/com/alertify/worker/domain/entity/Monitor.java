package com.alertify.worker.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Monitor {
    private UUID id;
    private String sourceType;
    private Map<String, String> params;
    private List<Rule> rules;
}