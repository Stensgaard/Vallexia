package com.vallexia.store.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * Request DTO for creating a store (admin-only).
 */
@Data
public class AdminCreateStoreRequestDto {

  @NotBlank(message = "name is required")
  @Size(max = 100, message = "name must be <= 100 chars")
  private String name;

  @NotBlank(message = "displayName is required")
  @Size(max = 100, message = "displayName must be <= 100 chars")
  private String displayName;

  @NotBlank(message = "flyerUrl is required")
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

