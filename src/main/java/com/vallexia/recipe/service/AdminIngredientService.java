package com.vallexia.recipe.service;

import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.recipe.dto.admin.AdminAddIngredientAliasRequestDto;
import com.vallexia.recipe.dto.admin.AdminCreateIngredientRequestDto;
import com.vallexia.recipe.dto.admin.AdminCreateIngredientResponseDto;
import com.vallexia.recipe.dto.admin.AdminIngredientSearchResultDto;
import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.IngredientTranslation;
import com.vallexia.recipe.repository.IngredientRepository;
import com.vallexia.recipe.repository.IngredientTranslationRepository;
import com.vallexia.store.entity.IngredientAlias;
import com.vallexia.store.repository.IngredientAliasRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminIngredientService {

  private final IngredientRepository ingredientRepository;
  private final IngredientTranslationRepository ingredientTranslationRepository;
  private final IngredientAliasRepository ingredientAliasRepository;

  @Transactional(readOnly = true)
  public List<AdminIngredientSearchResultDto> search(String q, String locale, int limit) {
    String query = q == null ? "" : q.trim();
    if (query.isBlank()) {
      return List.of();
    }

    String loc = normalizeLocale(locale);
    int lim = Math.max(1, Math.min(limit, 100));

    // Prefer translation search for the chosen locale.
    List<IngredientTranslation> translations =
        ingredientTranslationRepository.findByLocaleAndNameContainsIgnoreCase(loc, query);

    List<AdminIngredientSearchResultDto> results = new ArrayList<>();
    for (IngredientTranslation t : translations) {
      results.add(new AdminIngredientSearchResultDto(
          t.getIngredient().getId(),
          t.getIngredient().getName(),
          t.getName()));
      if (results.size() >= lim) {
        return results;
      }
    }

    // Also search canonical names (useful if locale translation missing).
    List<Ingredient> canonical = ingredientRepository.findTop50ByNameContainingIgnoreCase(query);
    for (Ingredient i : canonical) {
      boolean already = results.stream().anyMatch(r -> r.getIngredientId().equals(i.getId()));
      if (already) {
        continue;
      }

      String localized = ingredientTranslationRepository.findByIngredientIdAndLocale(i.getId(), loc)
          .map(IngredientTranslation::getName)
          .orElse(i.getName());

      results.add(new AdminIngredientSearchResultDto(i.getId(), i.getName(), localized));
      if (results.size() >= lim) {
        break;
      }
    }

    return results;
  }

  @Transactional
  public AdminCreateIngredientResponseDto createIngredient(AdminCreateIngredientRequestDto req) {
    if (req == null) {
      throw new IllegalArgumentException("Request is required");
    }
    String canonical = req.getCanonicalName() != null ? req.getCanonicalName().trim() : "";
    if (canonical.isBlank()) {
      throw new IllegalArgumentException("canonicalName is required");
    }

    Ingredient ingredient = new Ingredient();
    ingredient.setName(canonical);

    try {
      ingredient = ingredientRepository.save(ingredient);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalArgumentException("Ingredient already exists: " + canonical);
    }

    Map<String, String> createdTranslations = new HashMap<>();
    if (req.getTranslations() != null) {
      for (Map.Entry<String, String> entry : req.getTranslations().entrySet()) {
        String loc = normalizeLocale(entry.getKey());
        String name = entry.getValue() != null ? entry.getValue().trim() : "";
        if (name.isBlank()) {
          continue;
        }

        IngredientTranslation t = new IngredientTranslation();
        t.setIngredient(ingredient);
        t.setLocale(loc);
        t.setName(name);
        try {
          ingredientTranslationRepository.save(t);
          createdTranslations.put(loc, name);
        } catch (DataIntegrityViolationException e) {
          // ignore duplicates for the same ingredient+locale
        }
      }
    }

    return new AdminCreateIngredientResponseDto(ingredient.getId(), ingredient.getName(), createdTranslations);
  }

  @Transactional
  public void addAlias(Long ingredientId, AdminAddIngredientAliasRequestDto req) {
    if (ingredientId == null) {
      throw new IllegalArgumentException("ingredientId is required");
    }
    if (req == null) {
      throw new IllegalArgumentException("Request is required");
    }

    Ingredient ingredient = ingredientRepository.findById(ingredientId)
        .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + ingredientId));

    String loc = normalizeLocale(req.getLocale());
    String aliasText = req.getAlias() != null ? req.getAlias().trim() : "";
    if (aliasText.isBlank()) {
      throw new IllegalArgumentException("alias is required");
    }
    int priority = req.getPriority() != null ? req.getPriority() : 0;

    IngredientAlias alias = new IngredientAlias();
    alias.setIngredient(ingredient);
    alias.setLocale(loc);
    alias.setAlias(aliasText);
    alias.setPriority(priority);

    try {
      ingredientAliasRepository.save(alias);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalArgumentException("Alias already exists for locale: " + loc + ", alias: " + aliasText);
    }
  }

  private String normalizeLocale(String locale) {
    // Use your SupportedLocale validator semantics: accept unknown -> fallback to EN
    String code = SupportedLocale.fromCode(locale)
        .map(SupportedLocale::getCode)
        .orElse(SupportedLocale.EN.getCode());
    // normalize "da-DK" style
    String l = code.toLowerCase(Locale.ROOT);
    if (l.contains("-")) {
      l = l.substring(0, l.indexOf('-'));
    }
    return l;
  }
}

