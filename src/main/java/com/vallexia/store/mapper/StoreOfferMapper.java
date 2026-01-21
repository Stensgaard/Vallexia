package com.vallexia.store.mapper;

import com.vallexia.store.dto.StoreDto;
import com.vallexia.store.dto.StoreOfferDto;
import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Arrays;
import java.util.List;

/**
 * MapStruct mapper for converting between Store and StoreOffer entities and DTOs.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Mapper(componentModel = "spring")
public interface StoreOfferMapper {
    
    /**
     * Convert Store entity to StoreDto.
     * 
     * @param store Store entity
     * @return StoreDto
     */
    @Mapping(target = "foodFlyerKeywords", ignore = true)
    StoreDto toStoreDto(Store store);
    
    /**
     * Set the foodFlyerKeywords field after mapping.
     * 
     * @param store the source entity
     * @param dto the target DTO
     */
    @AfterMapping
    default void setFoodFlyerKeywords(Store store, @MappingTarget StoreDto dto) {
        if (store != null && dto != null && store.getFoodFlyerKeywords() != null) {
            dto.setFoodFlyerKeywords(Arrays.asList(store.getFoodFlyerKeywords()));
        }
    }
    
    /**
     * Convert list of Store entities to list of StoreDtos.
     * 
     * @param stores list of Store entities
     * @return list of StoreDtos
     */
    List<StoreDto> toStoreDtoList(List<Store> stores);
    
    /**
     * Convert StoreOffer entity to StoreOfferDto.
     * 
     * @param storeOffer StoreOffer entity
     * @return StoreOfferDto
     */
    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeName", source = "store.displayName")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "bundlePrice", source = "bundlePrice")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "minPurchaseQty", source = "minPurchaseQty")
    @Mapping(target = "minPurchaseUnit", source = "minPurchaseUnit")
    @Mapping(target = "packageQtyMin", source = "packageQtyMin")
    @Mapping(target = "packageQtyMax", source = "packageQtyMax")
    @Mapping(target = "packageUnit", source = "packageUnit")
    @Mapping(target = "valid", ignore = true)
    StoreOfferDto toStoreOfferDto(StoreOffer storeOffer);
    
    /**
     * Set the isValid field after mapping.
     * 
     * @param storeOffer the source entity
     * @param dto the target DTO
     */
    @AfterMapping
    default void setValidStatus(StoreOffer storeOffer, @MappingTarget StoreOfferDto dto) {
        if (storeOffer != null && dto != null) {
            dto.setValid(storeOffer.isValid());
        }
    }
    
    /**
     * Convert list of StoreOffer entities to list of StoreOfferDtos.
     * 
     * @param storeOffers list of StoreOffer entities
     * @return list of StoreOfferDtos
     */
    List<StoreOfferDto> toStoreOfferDtoList(List<StoreOffer> storeOffers);
}
