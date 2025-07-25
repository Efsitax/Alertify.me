package com.alertify.monitorservice.scheduler;

import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.scheduler.model.MetricSample;

public interface MetricFetcherPort {
    MetricSample fetch(Monitor monitor);
}
