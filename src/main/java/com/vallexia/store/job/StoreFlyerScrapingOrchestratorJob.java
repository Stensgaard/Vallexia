package com.vallexia.store.job;

import com.vallexia.config.store.StoreScrapingProperties;
import com.vallexia.store.entity.Store;
import com.vallexia.store.repository.StoreRepository;
import com.vallexia.store.service.StoreFlyerScrapingService;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

/**
 * Orchestrator job that scrapes only the stores that are due.
 *
 * <p>This scales better than a single "scrape all stores" job because each store can have its own
 * schedule and failures can be backoff/retried without blocking unrelated stores.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "app.store-scrape",
    name = "orchestrator-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class StoreFlyerScrapingOrchestratorJob {

  private final StoreRepository storeRepository;
  private final StoreFlyerScrapingService scrapingService;
  private final StoreScrapingProperties properties;

  @Scheduled(fixedDelayString = "${app.store-scrape.orchestrator-fixed-delay-ms:900000}")
  @SchedulerLock(name = "storeFlyerScrapingOrchestrator", lockAtLeastFor = "PT30S")
  public void run() {
    OffsetDateTime now = OffsetDateTime.now();

    List<Store> dueStores = storeRepository.findStoresDueForScraping(now);
    if (dueStores.isEmpty()) {
      return;
    }

    int processed = 0;
    for (Store store : dueStores) {
      if (processed >= properties.getMaxStoresPerRun()) {
        log.info(
            "Reached max stores per run ({}). Remaining due stores will be handled next cycle.",
            properties.getMaxStoresPerRun());
        break;
      }

      if (store.getNextScrapeAt() == null) {
        // Initialize schedule for newly created stores without scraping immediately.
        OffsetDateTime next = calculateNextScrapeAt(store, now.plusSeconds(1));
        store.setNextScrapeAt(next);
        storeRepository.save(store);
        processed++;
        continue;
      }

      scrapeOneStore(store);
      processed++;

      if (properties.getDelayBetweenStoresMs() > 0) {
        try {
          Thread.sleep(properties.getDelayBetweenStoresMs());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.warn("Orchestrator interrupted while waiting between stores");
          break;
        }
      }
    }
  }

  private void scrapeOneStore(Store store) {
    OffsetDateTime startedAt = OffsetDateTime.now();
    store.setLastScrapedAt(startedAt);

    try {
      int offersCount = scrapingService.scrapeStoreOffers(store);

      store.setConsecutiveFailures(0);
      store.setLastScrapeError(null);
      store.setNextScrapeAt(calculateNextScrapeAt(store, startedAt.plusSeconds(1)));
      storeRepository.save(store);

      log.info(
          "Store scraping succeeded. store={} offers={} nextScrapeAt={}",
          store.getName(),
          offersCount,
          store.getNextScrapeAt());
    } catch (Exception e) {
      int failures = store.getConsecutiveFailures() + 1;
      store.setConsecutiveFailures(failures);
      store.setLastScrapeError(safeErrorMessage(e));
      store.setNextScrapeAt(startedAt.plusMinutes(calculateBackoffMinutes(failures)));
      storeRepository.save(store);

      log.warn(
          "Store scraping failed. store={} failures={} nextScrapeAt={}",
          store.getName(),
          failures,
          store.getNextScrapeAt(),
          e);
    }
  }

  private OffsetDateTime calculateNextScrapeAt(Store store, OffsetDateTime from) {
    String cron = store.getScrapeCron();
    String zoneId = store.getScrapeZone();

    try {
      CronExpression expression = CronExpression.parse(cron);
      ZoneId zone = ZoneId.of(zoneId);

      ZonedDateTime fromZoned = from.atZoneSameInstant(zone);
      ZonedDateTime next = expression.next(fromZoned);
      if (next == null) {
        return from.plusDays(7);
      }
      return next.toOffsetDateTime();
    } catch (Exception e) {
      // If schedule config is invalid, back off and log. Keep store enabled so it can be fixed.
      log.error("Invalid scheduling config for store {} (cron='{}', zone='{}')", store.getName(), cron, zoneId, e);
      return from.plusHours(6);
    }
  }

  private int calculateBackoffMinutes(int consecutiveFailures) {
    int base = properties.getFailureBackoffBaseMinutes();
    int max = properties.getFailureBackoffMaxMinutes();

    // base * 2^(failures-1), capped at max
    long multiplier = 1L << Math.min(consecutiveFailures - 1, 30);
    long backoff = (long) base * multiplier;
    return (int) Math.min(backoff, max);
  }

  private String safeErrorMessage(Exception e) {
    String message = e.getMessage();
    if (message == null || message.isBlank()) {
      message = e.getClass().getSimpleName();
    }
    // Avoid unbounded DB growth
    int maxLen = 2000;
    return message.length() <= maxLen ? message : message.substring(0, maxLen);
  }
}
