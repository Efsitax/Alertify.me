package com.alertify.fetcher.domain.port;

import com.alertify.fetcher.domain.model.SiteConfig;

import java.util.List;

public interface SiteSpecificFetcher extends MetricFetcher {

    boolean supportsDomain(String domain);

    String getSiteName();

    List<String> getSupportedDomains();

    SiteConfig getConfiguration();

    default int getPriority() {
        return 100;
    }

    default boolean requiresJavaScript() {
        return getConfiguration().isRequiresJs();
    }

    default boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        return getSupportedDomains().stream()
                .anyMatch(domain -> url.toLowerCase().contains(domain.toLowerCase()));
    }
}