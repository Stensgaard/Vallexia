package com.vallexia.store.repository;

import com.vallexia.store.entity.StoreOfferIngredientMatch;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for offer->ingredient matches.
 */
@Repository
public interface StoreOfferIngredientMatchRepository extends JpaRepository<StoreOfferIngredientMatch, Long> {

  @Query("""
      select m
      from StoreOfferIngredientMatch m
      join fetch m.offer o
      join fetch o.store s
      join fetch m.ingredient i
      where o.id = :offerId
      """)
  Optional<StoreOfferIngredientMatch> findByOfferIdFetchAll(@Param("offerId") Long offerId);

  @Query("""
      select m
      from StoreOfferIngredientMatch m
      join fetch m.offer o
      join fetch o.store s
      join fetch m.ingredient i
      where :date between o.validFrom and o.validTo
      """)
  List<StoreOfferIngredientMatch> findAllCurrentMatches(@Param("date") LocalDate date);

  @Query("""
      select m
      from StoreOfferIngredientMatch m
      join fetch m.offer o
      join fetch o.store s
      join fetch m.ingredient i
      where :date between o.validFrom and o.validTo
        and s.id in :storeIds
      """)
  List<StoreOfferIngredientMatch> findCurrentMatchesByStoreIds(
      @Param("date") LocalDate date, @Param("storeIds") List<Long> storeIds);
}

