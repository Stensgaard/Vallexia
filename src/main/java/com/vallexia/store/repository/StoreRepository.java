package com.vallexia.store.repository;

import com.vallexia.store.entity.Store;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Find stores that are due for scraping.
     * Includes stores with no nextScrapeAt yet (to allow initialization).
     */
    @Query("""
        select s
        from Store s
        where s.scrapeEnabled = true
          and (s.nextScrapeAt is null or s.nextScrapeAt <= :now)
        order by
          case when s.nextScrapeAt is null then 0 else 1 end,
          s.nextScrapeAt asc,
          s.id asc
        """)
    List<Store> findStoresDueForScraping(@Param("now") OffsetDateTime now);
}
