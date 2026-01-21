package com.vallexia.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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

    /**
     * Bundle price as shown in flyer when multi-buy applies (e.g. "2 stk for 30").
     * When present, this usually equals {@link #price}.
     */
    @Column(name = "bundle_price", precision = 10, scale = 2)
    private BigDecimal bundlePrice;

    /**
     * Derived per-unit price when minPurchaseQty is known.
     */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Minimum purchase quantity for the bundle price (e.g. 2).
     */
    @Column(name = "min_purchase_qty")
    private Integer minPurchaseQty;

    /**
     * Unit for the minimum purchase (e.g. "stk", "pakke").
     */
    @Column(name = "min_purchase_unit", length = 20)
    private String minPurchaseUnit;

    /**
     * Original price text from the flyer page (useful for debugging/analytics).
     */
    @Column(name = "raw_price_text", length = 255)
    private String rawPriceText;

    /**
     * Extracted package size minimum quantity (e.g., 400 g -> 400, 1.6-2.8 kg -> 1.6).
     */
    @Column(name = "package_qty_min", precision = 10, scale = 3)
    private BigDecimal packageQtyMin;

    /**
     * Extracted package size maximum quantity for ranges (e.g., 1.6-2.8 kg -> 2.8).
     * Null when the size is a single value.
     */
    @Column(name = "package_qty_max", precision = 10, scale = 3)
    private BigDecimal packageQtyMax;

    /**
     * Extracted package size unit (e.g., g, kg, ml, cl, l).
     */
    @Column(name = "package_unit", length = 10)
    private String packageUnit;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;
    
    @Column(name = "scraped_at", nullable = false, updatable = false)
    private LocalDateTime scrapedAt;

    /**
     * Whether this offer has been dismissed by an admin (excluded from matching/unmatched list).
     */
    @Column(name = "dismissed", nullable = false)
    private boolean dismissed = false;

    /**
     * Timestamp when the offer was dismissed.
     */
    @Column(name = "dismissed_at")
    private OffsetDateTime dismissedAt;
    
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
