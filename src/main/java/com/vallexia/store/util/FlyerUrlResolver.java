package com.vallexia.store.util;

import com.vallexia.store.entity.Store;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility component for resolving flyer URLs and detecting landing pages.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Component
@Slf4j
public class FlyerUrlResolver {

    /**
     * Check if the page is a landing page with multiple flyer links.
     * Detects by finding multiple links that go to different subdomains (e.g., avis.bilka.dk).
     * 
     * @param doc the HTML document
     * @return true if page appears to be a landing page with multiple flyers
     */
    public boolean isLandingPage(Document doc) {
        Elements contentLinks = doc.select("main a[href], .content a[href], .main-content a[href], " +
                                           ".flyer a[href], .flyer-preview a[href], .carousel a[href], " +
                                           ".swiper a[href]");
        
        if (contentLinks.isEmpty()) {
            contentLinks = doc.select("a[href]");
        }
        
        Set<String> distinctHosts = new HashSet<>();
        String baseHost = getHost(doc.baseUri());
        
        for (Element link : contentLinks) {
            if (isInExcludedArea(link)) {
                continue;
            }
            
            String href = link.attr("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            
            try {
                String fullUrl = buildFullUrl(href, doc.baseUri());
                String host = getHost(fullUrl);
                
                // Count distinct subdomains different from base (e.g., avis.bilka.dk vs www.bilka.dk)
                if (host != null && !host.equals(baseHost) && !distinctHosts.contains(host)) {
                    distinctHosts.add(host);

                    // Treat "avis.*" flyer hosts as landing page indicator even if it's the only distinct host.
                    // This is needed for FOETEX where the landing page typically links to avis.foetex.dk only.
                    if (host.startsWith("avis.")) {
                        return true;
                    }
                    
                    if (distinctHosts.size() >= 2) {
                        return true;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        return false;
    }
    
    /**
     * Find the food flyer URL from a landing page using store-specific keywords.
     * 
     * @param landingPage the HTML document of the landing page
     * @param store the store entity with keywords
     * @return the food flyer URL if found, null otherwise
     */
    public String findFoodFlyerUrl(Document landingPage, Store store) {
        List<String> keywords = store.getFoodFlyerKeywords() != null
            ? Arrays.asList(store.getFoodFlyerKeywords())
            : List.of();
        Elements allLinks = landingPage.select("a[href]");
        
        // Prefer keyword-based matching when configured, otherwise fall back to picking an avis.* link.
        for (Element link : allLinks) {
            // Skip links in excluded areas
            if (isInExcludedArea(link)) {
                continue;
            }
            
            String href = link.attr("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            
            String fullUrl = buildFullUrl(href, landingPage.baseUri());
            String lowerUrl = fullUrl.toLowerCase();
            
            if (!keywords.isEmpty()) {
                // Check if URL contains any of the keywords
                boolean matchesKeyword = keywords.stream()
                    .anyMatch(keyword -> lowerUrl.contains(keyword.toLowerCase()));
                
                if (matchesKeyword) {
                    log.debug("Found food flyer link matching keywords {}: {}", keywords, fullUrl);
                    return fullUrl;
                }
            }

            // Fallback: if no keywords configured or no match, accept an avis.* flyer host
            try {
                String host = getHost(fullUrl);
                if (host != null && host.startsWith("avis.")) {
                    return fullUrl;
                }
            } catch (Exception ignored) {}
        }
        
        log.debug("No food flyer link found (keywords: {}) for store: {}", keywords, store.getName());
        return null;
    }
    
    /**
     * Build a full URL from a relative or absolute URL.
     * 
     * @param href the URL (may be relative or absolute)
     * @param baseUrl the base URL to use for relative URLs
     * @return the full absolute URL
     */
    public String buildFullUrl(String href, String baseUrl) {
        if (href == null || href.isEmpty()) {
            return href;
        }
        
        // If already absolute, return as-is
        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }
        
        // Handle relative URLs
        try {
            URI baseUri = new URI(baseUrl);
            URI resolvedUri = baseUri.resolve(href);
            return resolvedUri.toString();
        } catch (Exception e) {
            log.debug("Failed to build full URL from href: {} and base: {}", href, baseUrl, e);
            // Fallback: simple concatenation
            if (href.startsWith("/")) {
                URI baseUri = URI.create(baseUrl);
                return baseUri.getScheme() + "://" + baseUri.getHost() + href;
            } else {
                return baseUrl + "/" + href;
            }
        }
    }
    
    /**
     * Extract host from URL.
     */
    private String getHost(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if an element is in an excluded area (footer, sidebar, navigation).
     * 
     * @param element the element to check
     * @return true if element is in an excluded area
     */
    private boolean isInExcludedArea(Element element) {
        Element current = element;
        while (current != null && !current.tagName().equals("body") && !current.tagName().equals("html")) {
            String tagName = current.tagName().toLowerCase();
            String className = current.className().toLowerCase();
            String id = current.id().toLowerCase();
            
            // Check for excluded tags
            if (tagName.equals("footer") || tagName.equals("nav") || tagName.equals("aside")) {
                return true;
            }
            
            // Check for excluded classes/IDs
            if (className.contains("footer") || className.contains("sidebar") || 
                className.contains("side-bar") || className.contains("navigation") ||
                id.contains("footer") || id.contains("sidebar") || id.contains("nav")) {
                return true;
            }
            
            current = current.parent();
        }
        
        return false;
    }
}

