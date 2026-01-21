package com.vallexia.store.service;

import com.vallexia.store.dto.admin.AdminCreateStoreRequestDto;
import com.vallexia.store.dto.admin.AdminStoreDto;
import com.vallexia.store.dto.admin.AdminUpdateStoreRequestDto;
import com.vallexia.store.entity.Store;
import com.vallexia.store.repository.StoreRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStoreService {

  private final StoreRepository storeRepository;

  @Transactional(readOnly = true)
  public List<AdminStoreDto> listStores() {
    return storeRepository.findAll().stream()
        .sorted(Comparator.comparing(Store::getDisplayName, Comparator.nullsLast(String::compareToIgnoreCase)))
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public AdminStoreDto create(AdminCreateStoreRequestDto req) {
    if (req == null) {
      throw new IllegalArgumentException("Request is required");
    }
    String name = trim(req.getName());
    String displayName = trim(req.getDisplayName());
    String flyerUrl = trim(req.getFlyerUrl());
    String websiteUrl = trim(req.getWebsiteUrl());

    if (name.isBlank()) {
      throw new IllegalArgumentException("name is required");
    }
    if (displayName.isBlank()) {
      throw new IllegalArgumentException("displayName is required");
    }
    if (flyerUrl.isBlank()) {
      throw new IllegalArgumentException("flyerUrl is required");
    }

    Store s = new Store();
    s.setName(name.toUpperCase(Locale.ROOT));
    s.setDisplayName(displayName);
    s.setFlyerUrl(flyerUrl);
    s.setWebsiteUrl(websiteUrl.isBlank() ? null : websiteUrl);
    s.setFoodFlyerKeywords(toKeywordsArray(req.getFoodFlyerKeywords()));

    if (req.getScrapeEnabled() != null) {
      s.setScrapeEnabled(req.getScrapeEnabled());
    }
    if (req.getScrapeCron() != null && !req.getScrapeCron().trim().isBlank()) {
      s.setScrapeCron(req.getScrapeCron().trim());
    }
    if (req.getScrapeZone() != null && !req.getScrapeZone().trim().isBlank()) {
      s.setScrapeZone(req.getScrapeZone().trim());
    }

    try {
      s = storeRepository.save(s);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalArgumentException("Store already exists: " + s.getName());
    }

    return toDto(s);
  }

  @Transactional
  public AdminStoreDto update(Long storeId, AdminUpdateStoreRequestDto req) {
    if (storeId == null) {
      throw new IllegalArgumentException("storeId is required");
    }
    if (req == null) {
      throw new IllegalArgumentException("Request is required");
    }

    Store s = storeRepository.findById(storeId)
        .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

    if (req.getDisplayName() != null) {
      String v = req.getDisplayName().trim();
      if (v.isBlank()) {
        throw new IllegalArgumentException("displayName cannot be blank");
      }
      s.setDisplayName(v);
    }
    if (req.getFlyerUrl() != null) {
      String v = req.getFlyerUrl().trim();
      if (v.isBlank()) {
        throw new IllegalArgumentException("flyerUrl cannot be blank");
      }
      s.setFlyerUrl(v);
    }
    if (req.getWebsiteUrl() != null) {
      String v = req.getWebsiteUrl().trim();
      s.setWebsiteUrl(v.isBlank() ? null : v);
    }
    if (req.getFoodFlyerKeywords() != null) {
      s.setFoodFlyerKeywords(toKeywordsArray(req.getFoodFlyerKeywords()));
    }
    if (req.getScrapeEnabled() != null) {
      s.setScrapeEnabled(req.getScrapeEnabled());
    }
    if (req.getScrapeCron() != null) {
      String v = req.getScrapeCron().trim();
      if (v.isBlank()) {
        throw new IllegalArgumentException("scrapeCron cannot be blank");
      }
      s.setScrapeCron(v);
    }
    if (req.getScrapeZone() != null) {
      String v = req.getScrapeZone().trim();
      if (v.isBlank()) {
        throw new IllegalArgumentException("scrapeZone cannot be blank");
      }
      s.setScrapeZone(v);
    }

    return toDto(storeRepository.save(s));
  }

  private AdminStoreDto toDto(Store s) {
    List<String> keywords = s.getFoodFlyerKeywords() == null
        ? List.of()
        : Arrays.stream(s.getFoodFlyerKeywords())
            .filter(k -> k != null && !k.isBlank())
            .map(String::trim)
            .collect(Collectors.toList());

    return new AdminStoreDto(
        s.getId(),
        s.getName(),
        s.getDisplayName(),
        s.getFlyerUrl(),
        s.getWebsiteUrl(),
        keywords,
        s.isScrapeEnabled(),
        s.getScrapeCron(),
        s.getScrapeZone(),
        s.getNextScrapeAt(),
        s.getLastScrapedAt(),
        s.getConsecutiveFailures(),
        s.getLastScrapeError());
  }

  private String[] toKeywordsArray(List<String> keywords) {
    if (keywords == null) {
      return null;
    }
    List<String> cleaned = keywords.stream()
        .filter(k -> k != null && !k.trim().isBlank())
        .map(k -> k.trim())
        .distinct()
        .collect(Collectors.toList());
    return cleaned.isEmpty() ? null : cleaned.toArray(new String[0]);
  }

  private String trim(String s) {
    return s == null ? "" : s.trim();
  }
}

