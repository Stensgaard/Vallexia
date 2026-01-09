package com.vallexia.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a weekly offer from a store's flyer.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Entity
@Table(name = "store_offers", indexes = {
    @Index(name = "idx_store_offers_store_id", columnList = "store_id"),
    @Index(name = "idx_store_offers_valid_dates", columnList = "valid_from,valid_to")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreOffer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;
    
    @Column(name = "scraped_at", nullable = false, updatable = false)
    private LocalDateTime scrapedAt;
    
    /**
     * Check if this offer is currently valid.
     * 
     * @return true if offer is valid today, false otherwise
     */
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(validFrom) && !today.isAfter(validTo);
    }
}

