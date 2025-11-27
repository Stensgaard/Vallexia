package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.NutritionalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for NutritionalInfo entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Repository
public interface NutritionalInfoRepository extends JpaRepository<NutritionalInfo, Long> {
}
