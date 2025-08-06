package com.alertify.fetcher.adapter.out.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class SeleniumWebDriverService {

    private WebDriver driver;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing Chrome WebDriver...");

            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            log.info("Chrome WebDriver initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialized WebDriver: {}", e.getMessage(), e);
            throw new RuntimeException("WebDriver initialization failed", e);
        }
    }

    public String fetchPageSource(String url) {
        try {
            log.info("Fetching page source with Selenium for URL: {}", url);

            driver.get(url);

            Thread.sleep(3000);

            String pageSource = driver.getPageSource();
            log.info("Successfully fetched {} characters with Selenium from {}",
                    pageSource.length(), url);

            return pageSource;
        } catch (Exception e) {
            log.error("Failed to fetch page source for URL {}: {}", url, e.getMessage());
            throw new RuntimeException("Selenium fetch failed", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (driver != null) {
            log.info("Closing WebDriver...");
            driver.quit();
        }
    }
}
