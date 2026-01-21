package com.vallexia.recipe.controller;

import com.vallexia.recipe.dto.admin.AdminAddIngredientAliasRequestDto;
import com.vallexia.recipe.dto.admin.AdminCreateIngredientRequestDto;
import com.vallexia.recipe.dto.admin.AdminCreateIngredientResponseDto;
import com.vallexia.recipe.dto.admin.AdminIngredientSearchResultDto;
import com.vallexia.recipe.service.AdminIngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints for ingredient curation (search/create/aliases).
 */
@RestController
@RequestMapping("/api/v1/admin/ingredients")
@Tag(name = "Admin Ingredients", description = "Admin operations for ingredient curation")
@RequiredArgsConstructor
public class AdminIngredientController {

  private final AdminIngredientService adminIngredientService;

  @Operation(summary = "Search ingredients (Admin only)")
  @GetMapping("/search")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<AdminIngredientSearchResultDto>> search(
      @RequestParam String q,
      @RequestParam(required = false) String locale,
      @RequestParam(required = false, defaultValue = "20") int limit) {
    return ResponseEntity.ok(adminIngredientService.search(q, locale, limit));
  }

  @Operation(summary = "Create ingredient (Admin only)")
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AdminCreateIngredientResponseDto> create(
      @Valid @RequestBody AdminCreateIngredientRequestDto req) {
    return ResponseEntity.ok(adminIngredientService.createIngredient(req));
  }

  @Operation(summary = "Add ingredient alias (Admin only)")
  @PostMapping("/{ingredientId}/aliases")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> addAlias(
      @PathVariable Long ingredientId,
      @Valid @RequestBody AdminAddIngredientAliasRequestDto req) {
    adminIngredientService.addAlias(ingredientId, req);
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("ingredientId", ingredientId);
    return ResponseEntity.ok(response);
  }
}

