package com.vallexia.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity representing a configurable exclusion rule for filtering store offers during scraping.
 * Rules can be global (apply to all stores) or store-specific.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Entity
@Table(name = "store_offer_exclusion_rule", indexes = {
    @Index(name = "idx_store_offer_exclusion_rule_lookup", columnList = "enabled,scope,store_name,priority")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreOfferExclusionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "scope", nullable = false, length = 20)
    private String scope; // "GLOBAL" or "STORE"

    @Column(name = "store_name", length = 100)
    private String storeName; // Only set when scope = "STORE"

    @Column(name = "match_type", nullable = false, length = 20)
    private String matchType; // "WORD", "CONTAINS", or "REGEX"

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "patterns", nullable = false, columnDefinition = "TEXT[]")
    private String[] patterns;

    @Column(name = "priority", nullable = false)
    private int priority = 100;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    /**
     * Check if this rule applies to the given store.
     * 
     * @param storeName the store name to check
     * @return true if rule applies (global or store-specific match)
     */
    public boolean appliesTo(String storeName) {
        if (!enabled) {
            return false;
        }
        if ("GLOBAL".equals(scope)) {
            return true;
        }
        if ("STORE".equals(scope)) {
            return storeName != null && storeName.equals(this.storeName);
        }
        return false;
    }
}
