package com.vallexia.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for store information.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {
    
    private Long id;
    
    private String name;
    
    private String displayName;
    
    private String flyerUrl;
    
    private String websiteUrl;
    
    private List<String> foodFlyerKeywords;
}
