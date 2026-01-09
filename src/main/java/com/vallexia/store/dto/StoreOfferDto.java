package com.vallexia.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for store offers.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreOfferDto {
    
    private Long id;
    
    private Long storeId;
    
    private String storeName;
    
    private String productName;

    /**
     * Normalized offer price.
     */
    private BigDecimal price;
    
    private LocalDate validFrom;
    
    private LocalDate validTo;
    
    private boolean isValid;
}

