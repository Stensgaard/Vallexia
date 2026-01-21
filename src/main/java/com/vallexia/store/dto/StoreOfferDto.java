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

    /**
     * Bundle price (when a minimum purchase quantity applies).
     */
    private BigDecimal bundlePrice;

    /**
     * Derived per-unit price when minPurchaseQty is known.
     */
    private BigDecimal unitPrice;

    private Integer minPurchaseQty;

    private String minPurchaseUnit;

    /**
     * Extracted package size minimum quantity (e.g., 400 g -> 400, 1.6-2.8 kg -> 1.6).
     */
    private BigDecimal packageQtyMin;

    /**
     * Extracted package size maximum quantity for ranges (e.g., 1.6-2.8 kg -> 2.8).
     * Null when the size is a single value.
     */
    private BigDecimal packageQtyMax;

    /**
     * Extracted package size unit (e.g., g, kg, ml, cl, l).
     */
    private String packageUnit;
    
    private LocalDate validFrom;
    
    private LocalDate validTo;
    
    private boolean isValid;
}

