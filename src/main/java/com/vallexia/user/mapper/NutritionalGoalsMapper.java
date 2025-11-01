package com.vallexia.user.mapper;

import com.vallexia.user.dto.NutritionalGoalsDto;
import com.vallexia.user.entity.NutritionalGoals;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between NutritionalGoals entity and DTO.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Mapper(componentModel = "spring")
public interface NutritionalGoalsMapper {
    
    /**
     * Convert NutritionalGoals entity to NutritionalGoalsDto.
     * 
     * @param nutritionalGoals NutritionalGoals entity
     * @return NutritionalGoalsDto
     */
    @Mapping(target = "userId", source = "user.id")
    NutritionalGoalsDto toNutritionalGoalsDto(NutritionalGoals nutritionalGoals);
    
    /**
     * Convert NutritionalGoalsDto to NutritionalGoals entity.
     * Note: Percentage fields are ignored as they are calculated, not set directly.
     * 
     * @param nutritionalGoalsDto NutritionalGoalsDto
     * @return NutritionalGoals entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "proteinPercentage", ignore = true)
    @Mapping(target = "carbsPercentage", ignore = true)
    @Mapping(target = "fatsPercentage", ignore = true)
    NutritionalGoals toNutritionalGoals(NutritionalGoalsDto nutritionalGoalsDto);
}
