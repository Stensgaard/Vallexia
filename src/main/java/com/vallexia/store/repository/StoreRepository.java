package com.vallexia.store.repository;

import com.vallexia.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for store chain entities.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    /**
     * Find a store by its name.
     * 
     * @param name the store name (e.g., BILKA, NETTO, FOETEX)
     * @return Optional containing the store if found
     */
    Optional<Store> findByName(String name);
}


