package com.vallexia.config.store;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for store flyer scraping orchestration.
 *
 * <p>These settings control how often the orchestrator wakes up to pick up due stores and
 * how aggressively it retries failures.</p>
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.store-scrape")
public class StoreScrapingProperties {

  /**
   * Enable/disable the orchestrator scheduler.
   * Default: true
   */
  private boolean orchestratorEnabled = true;

  /**
   * How often (ms) the orchestrator checks for due stores.
   * Default: 900000 (15 minutes)
   */
  @Min(value = 60_000, message = "Orchestrator delay must be at least 60 seconds")
  private long orchestratorFixedDelayMs = 900_000;

  /**
   * Maximum number of stores processed per orchestrator run.
   * Default: 25
   */
  @Min(value = 1, message = "Max stores per run must be at least 1")
  @Max(value = 1_000, message = "Max stores per run must be at most 1000")
  private int maxStoresPerRun = 25;

  /**
   * Optional delay (ms) between stores to be polite.
   * Default: 0
   */
  @Min(value = 0, message = "Delay between stores must be >= 0")
  private long delayBetweenStoresMs = 0;

  /**
   * Base backoff (minutes) after a failure. Backoff grows exponentially with consecutive failures.
   * Default: 15
   */
  @Min(value = 1, message = "Failure backoff base must be at least 1 minute")
  private int failureBackoffBaseMinutes = 15;

  /**
   * Maximum backoff (minutes) after failures.
   * Default: 360 (6 hours)
   */
  @Min(value = 1, message = "Failure backoff max must be at least 1 minute")
  private int failureBackoffMaxMinutes = 360;
}
