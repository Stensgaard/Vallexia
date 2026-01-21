package com.vallexia.store.dto.admin;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * Request DTO for updating a store (admin-only).
 *
 * <p>All fields are optional; only provided fields are updated.</p>
 */
@Data
public class AdminUpdateStoreRequestDto {

  @Size(max = 100, message = "displayName must be <= 100 chars")
  private String displayName;

  @Size(max = 255, message = "flyerUrl must be <= 255 chars")
  private String flyerUrl;

  @Size(max = 255, message = "websiteUrl must be <= 255 chars")
  private String websiteUrl;

  private List<String> foodFlyerKeywords;

  private Boolean scrapeEnabled;

  @Size(max = 64, message = "scrapeCron must be <= 64 chars")
  private String scrapeCron;

  @Size(max = 64, message = "scrapeZone must be <= 64 chars")
  private String scrapeZone;
}

