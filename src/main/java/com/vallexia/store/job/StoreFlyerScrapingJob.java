package com.vallexia.store.job;

import com.vallexia.store.service.StoreFlyerScrapingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for scraping weekly flyer offers from store websites.
 * Runs every Monday at 2 AM when new weekly flyers are typically published.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreFlyerScrapingJob {
    
    private final StoreFlyerScrapingService scrapingService;
    
    /**
     * Scrape weekly offers from all store flyer pages.
     * Runs every Monday at 2 AM (when new weekly flyers are typically published).
     * 
     * Cron expression: "0 0 2 * * MON"
     * - 0: seconds (0)
     * - 0: minutes (0)
     * - 2: hours (2 AM)
     * - *: day of month (any)
     * - *: month (any)
     * - MON: day of week (Monday)
     */
    @Scheduled(cron = "0 0 2 * * MON")
    public void scrapeWeeklyOffers() {
        log.info("Starting scheduled store flyer scraping job");
        
        try {
            int totalOffers = scrapingService.scrapeAllStores();
            log.info("Scheduled store flyer scraping completed successfully. Total offers scraped: {}", totalOffers);
        } catch (Exception e) {
            log.error("Error during scheduled store flyer scraping job", e);
            // Don't throw exception - let the job complete even if some stores fail
            // Individual store errors are already logged in the service
        }
    }
}


