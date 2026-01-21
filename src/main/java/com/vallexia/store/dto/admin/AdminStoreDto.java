package com.vallexia.store.dto.admin;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin DTO for viewing/editing store configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStoreDto {

  private Long id;
  private String name;
  private String displayName;
  private String flyerUrl;
  private String websiteUrl;
  private List<String> foodFlyerKeywords;

  private boolean scrapeEnabled;
  private String scrapeCron;
  private String scrapeZone;
  private OffsetDateTime nextScrapeAt;
  private OffsetDateTime lastScrapedAt;
  private int consecutiveFailures;
  private String lastScrapeError;
}

