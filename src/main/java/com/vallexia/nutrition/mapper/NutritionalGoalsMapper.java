package com.vallexia.nutrition.mapper;

import com.vallexia.nutrition.dto.NutritionalGoalsDto;
import com.vallexia.nutrition.entity.NutritionalGoals;
import com.vallexia.nutrition.enums.GoalType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between NutritionalGoals entity and DTO.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-27
 */
@Mapper(componentModel = "spring")
public interface NutritionalGoalsMapper {
    
    /**
     * Convert GoalType enum to String.
     * 
     * @param goalType GoalType enum
     * @return String representation of the enum name, or null if goalType is null
     */
    @Named("goalTypeToString")
    default String goalTypeToString(GoalType goalType) {
        return goalType != null ? goalType.name() : null;
    }
    
    /**
     * Convert String to GoalType enum.
     * 
     * @param goalType String representation of goal type
     * @return GoalType enum, or null if goalType is null or empty
     */
    @Named("stringToGoalType")
    default GoalType stringToGoalType(String goalType) {
        if (goalType == null || goalType.isEmpty()) {
            return null;
        }
        try {
            return GoalType.valueOf(goalType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Convert NutritionalGoals entity to NutritionalGoalsDto.
     * 
     * @param nutritionalGoals NutritionalGoals entity
     * @return NutritionalGoalsDto
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "goalType", source = "goalType", qualifiedByName = "goalTypeToString")
    @Mapping(target = "proteinCalories", ignore = true)
    @Mapping(target = "carbCalories", ignore = true)
    @Mapping(target = "fatCalories", ignore = true)
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
    @Mapping(target = "goalType", source = "goalType", qualifiedByName = "stringToGoalType")
    NutritionalGoals toNutritionalGoals(NutritionalGoalsDto nutritionalGoalsDto);
}
