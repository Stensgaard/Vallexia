package com.vallexia.user.mapper;

import com.vallexia.user.dto.UserSettingsDto;
import com.vallexia.user.entity.UserSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between UserSettings entity and DTO.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
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
     * Note: numberDecimalSeparator and numberThousandsSeparator
     * are auto-populated from country in the service layer, so they are ignored here.
     * Currency can be overridden by the user, so it is mapped from the DTO.
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
    UserSettings toUserSettings(UserSettingsDto userSettingsDto);
}
