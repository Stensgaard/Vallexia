package com.vallexia.user.mapper;

import com.vallexia.user.dto.UserSettingsDto;
import com.vallexia.user.entity.UserSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between UserSettings entity and DTO.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Mapper(componentModel = "spring")
public interface UserSettingsMapper {
    
    /**
     * Convert UserSettings entity to UserSettingsDto.
     * 
     * @param userSettings UserSettings entity
     * @return UserSettingsDto
     */
    @Mapping(target = "userId", source = "user.id")
    UserSettingsDto toUserSettingsDto(UserSettings userSettings);
    
    /**
     * Convert UserSettingsDto to UserSettings entity.
     * Note: numberDecimalSeparator, numberThousandsSeparator, and currency
     * are auto-populated from country in the service layer, so they are ignored here.
     * 
     * @param userSettingsDto UserSettingsDto
     * @return UserSettings entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "numberDecimalSeparator", ignore = true)
    @Mapping(target = "numberThousandsSeparator", ignore = true)
    @Mapping(target = "currency", ignore = true)
    UserSettings toUserSettings(UserSettingsDto userSettingsDto);
}
