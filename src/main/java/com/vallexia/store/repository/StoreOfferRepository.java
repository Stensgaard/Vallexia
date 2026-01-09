package com.vallexia.store.repository;

import com.vallexia.store.entity.StoreOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for store offer entities.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Repository
public interface StoreOfferRepository extends JpaRepository<StoreOffer, Long> {
    
    /**
     * Find current offers for a specific store.
     * 
     * @param storeId the store ID
     * @param date the date to check validity against
     * @return list of current offers
     */
    @Query("SELECT o FROM StoreOffer o " +
           "JOIN FETCH o.store " +
           "WHERE o.store.id = :storeId AND :date BETWEEN o.validFrom AND o.validTo")
    List<StoreOffer> findCurrentOffersByStore(@Param("storeId") Long storeId, @Param("date") LocalDate date);
    
    /**
     * Delete offers for a store and validity period.
     * Used to clean up old offers before inserting new ones.
     * 
     * @param storeId the store ID
     * @param validFrom the validity start date
     */
    @Modifying
    @Query("DELETE FROM StoreOffer o WHERE o.store.id = :storeId AND o.validFrom = :validFrom")
    void deleteByStoreIdAndValidFrom(@Param("storeId") Long storeId, @Param("validFrom") LocalDate validFrom);
    
    /**
     * Find all current offers (valid today).
     * 
     * @param date the date to check validity against
     * @return list of all current offers
     */
    @Query("SELECT o FROM StoreOffer o " +
           "JOIN FETCH o.store " +
           "WHERE :date BETWEEN o.validFrom AND o.validTo")
    List<StoreOffer> findAllCurrentOffers(@Param("date") LocalDate date);
}

