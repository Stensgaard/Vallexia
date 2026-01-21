package com.vallexia.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a store chain with a public weekly flyer page.
 */
@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "flyer_url", nullable = false, length = 255)
    private String flyerUrl;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name = "food_flyer_keywords", columnDefinition = "TEXT[]")
    private String[] foodFlyerKeywords;

    /**
     * Whether automated scraping is enabled for this store.
     */
    @Column(name = "scrape_enabled", nullable = false)
    private boolean scrapeEnabled = true;

    /**
     * Cron expression for when this store's flyer should be scraped.
     * Uses Spring cron format.
     */
    @Column(name = "scrape_cron", nullable = false, length = 64)
    private String scrapeCron = "0 0 2 * * MON";

    /**
     * Time zone ID used for cron evaluation (e.g., Europe/Copenhagen).
     */
    @Column(name = "scrape_zone", nullable = false, length = 64)
    private String scrapeZone = "Europe/Copenhagen";

    /**
     * Computed next execution time for this store.
     */
    @Column(name = "next_scrape_at")
    private OffsetDateTime nextScrapeAt;

    /**
     * Timestamp of the last scraping attempt for this store.
     */
    @Column(name = "last_scraped_at")
    private OffsetDateTime lastScrapedAt;

    /**
     * Number of consecutive failures (used for backoff).
     */
    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures = 0;

    /**
     * Last scraping error message, if any.
     */
    @Column(name = "last_scrape_error")
    private String lastScrapeError;
}
