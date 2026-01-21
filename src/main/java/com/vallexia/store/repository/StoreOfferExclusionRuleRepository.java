package com.vallexia.store.repository;

import com.vallexia.store.entity.StoreOfferExclusionRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for store offer exclusion rules.
 */
@Repository
public interface StoreOfferExclusionRuleRepository extends JpaRepository<StoreOfferExclusionRule, Long> {

    /**
     * Find all enabled rules that apply to a given store (global + store-specific).
     * Results are ordered by priority (ascending) so lower priority rules are evaluated first.
     * 
     * @param storeName the store name (e.g., "NETTO", "BILKA")
     * @return list of applicable rules ordered by priority
     */
    @Query("SELECT r FROM StoreOfferExclusionRule r " +
           "WHERE r.enabled = true " +
           "AND ((r.scope = 'GLOBAL') OR (r.scope = 'STORE' AND r.storeName = :storeName)) " +
           "ORDER BY r.priority ASC")
    List<StoreOfferExclusionRule> findApplicableRules(@Param("storeName") String storeName);

    /**
     * Find all enabled rules (for admin listing).
     * 
     * @return list of all enabled rules ordered by priority, then by name
     */
    @Query("SELECT r FROM StoreOfferExclusionRule r " +
           "WHERE r.enabled = true " +
           "ORDER BY r.priority ASC, r.name ASC")
    List<StoreOfferExclusionRule> findAllEnabled();

    /**
     * Find all rules regardless of enabled status (for admin management).
     * 
     * @return list of all rules ordered by priority, then by name
     */
    @Query("SELECT r FROM StoreOfferExclusionRule r " +
           "ORDER BY r.priority ASC, r.name ASC")
    List<StoreOfferExclusionRule> findAllOrdered();
}
