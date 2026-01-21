package com.vallexia.store.controller;

import com.vallexia.store.dto.admin.AdminCreateStoreRequestDto;
import com.vallexia.store.dto.admin.AdminStoreDto;
import com.vallexia.store.dto.admin.AdminUpdateStoreRequestDto;
import com.vallexia.store.service.AdminStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints for managing stores.
 */
@RestController
@RequestMapping("/api/v1/admin/stores")
@Tag(name = "Admin Stores", description = "Admin operations for store management")
@RequiredArgsConstructor
public class AdminStoreController {

  private final AdminStoreService adminStoreService;

  @Operation(summary = "List stores (Admin only)")
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<AdminStoreDto>> listStores() {
    return ResponseEntity.ok(adminStoreService.listStores());
  }

  @Operation(summary = "Create store (Admin only)")
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AdminStoreDto> create(@Valid @RequestBody AdminCreateStoreRequestDto req) {
    return ResponseEntity.ok(adminStoreService.create(req));
  }

  @Operation(summary = "Update store (Admin only)")
  @PutMapping("/{storeId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AdminStoreDto> update(
      @PathVariable Long storeId,
      @Valid @RequestBody AdminUpdateStoreRequestDto req) {
    return ResponseEntity.ok(adminStoreService.update(storeId, req));
  }
}

