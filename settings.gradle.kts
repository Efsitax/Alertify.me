rootProject.name = "alertify"

include(
    "gateway",
    "libs:common-domain",
    "libs:common-web",
    "libs:common-utils",
    "libs:common-test",
    "services:monitor-service",
    "services:fetcher-service",
    "services:worker-service",
    "services:notification-service"
)