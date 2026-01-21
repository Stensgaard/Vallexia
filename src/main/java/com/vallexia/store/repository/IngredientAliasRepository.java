package com.vallexia.store.repository;

import com.vallexia.store.entity.IngredientAlias;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ingredient alias entities (locale-specific synonyms).
 */
@Repository
public interface IngredientAliasRepository extends JpaRepository<IngredientAlias, Long> {

  @Query("""
      select a
      from IngredientAlias a
      join fetch a.ingredient i
      where a.locale = :locale
        and lower(a.alias) = lower(:alias)
      order by a.priority desc, a.id asc
      """)
  Optional<IngredientAlias> findBestByLocaleAndAliasIgnoreCase(
      @Param("locale") String locale, @Param("alias") String alias);
}

