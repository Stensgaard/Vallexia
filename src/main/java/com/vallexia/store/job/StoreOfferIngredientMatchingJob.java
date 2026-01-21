package com.vallexia.store.job;

import com.vallexia.store.service.StoreOfferIngredientMatchingService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically matches newly scraped offers to canonical ingredients.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "app.store-scrape",
    name = "matching-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class StoreOfferIngredientMatchingJob {

  private final StoreOfferIngredientMatchingService matchingService;

  @Scheduled(fixedDelayString = "${app.store-scrape.matching-fixed-delay-ms:900000}")
  @SchedulerLock(name = "storeOfferIngredientMatchingJob", lockAtLeastFor = "PT30S")
  public void run() {
    try {
      matchingService.matchCurrentOffers(LocalDate.now());
    } catch (Exception e) {
      log.error("Error during store offer ingredient matching job", e);
    }
  }
}

