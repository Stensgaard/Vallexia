package com.vallexia.audit.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for securely extracting client IP addresses from HTTP requests.
 * Handles proxy headers with validation against trusted proxy list.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class IpAddressExtractor {
  
  private final Set<String> trustedProxies;
  
  /**
   * Constructor with configurable trusted proxies.
   * 
   * @param trustedProxiesConfig comma-separated list of trusted proxy IPs
   */
  public IpAddressExtractor(
      @Value("${app.audit.trusted-proxies:}") String trustedProxiesConfig) {
    this.trustedProxies = new HashSet<>();
    
    if (trustedProxiesConfig != null && !trustedProxiesConfig.isEmpty()) {
      Arrays.stream(trustedProxiesConfig.split(","))
          .map(String::trim)
          .filter(ip -> !ip.isEmpty())
          .forEach(trustedProxies::add);
    }
    
    log.info("Initialized IP extractor with {} trusted proxies", trustedProxies.size());
  }
  
  /**
   * Extracts the client IP address from the HTTP request.
   * If behind a trusted proxy, uses X-Forwarded-For or X-Real-IP headers.
   * Otherwise, uses the remote address.
   * 
   * @param request the HTTP request
   * @return the client IP address
   */
  public String extractClientIp(HttpServletRequest request) {
    String remoteAddr = request.getRemoteAddr();
    
    // If the request is from a trusted proxy, check forwarding headers
    if (isTrustedProxy(remoteAddr)) {
      String forwardedIp = extractFromForwardedHeader(request);
      if (forwardedIp != null) {
        log.debug("Using forwarded IP {} from trusted proxy {}", forwardedIp, remoteAddr);
        return forwardedIp;
      }
    }
    
    // Default to remote address
    return remoteAddr;
  }
  
  /**
   * Extracts both the client IP and proxy IP for complete audit trail.
   * 
   * @param request the HTTP request
   * @return formatted string with both IPs
   */
  public String extractFullIpInfo(HttpServletRequest request) {
    String remoteAddr = request.getRemoteAddr();
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    String xRealIp = request.getHeader("X-Real-IP");
    
    if (isTrustedProxy(remoteAddr)) {
      if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return String.format("%s (via proxy %s, X-Forwarded-For: %s)", 
            extractFromForwardedHeader(request), remoteAddr, xForwardedFor);
      } else if (xRealIp != null && !xRealIp.isEmpty()) {
        return String.format("%s (via proxy %s, X-Real-IP: %s)", 
            xRealIp, remoteAddr, xRealIp);
      }
    }
    
    return remoteAddr;
  }
  
  /**
   * Checks if the given IP is a trusted proxy.
   * 
   * @param ipAddress the IP address to check
   * @return true if trusted, false otherwise
   */
  private boolean isTrustedProxy(String ipAddress) {
    if (trustedProxies.isEmpty()) {
      // If no trusted proxies configured, don't trust any forwarding headers
      return false;
    }
    
    return trustedProxies.contains(ipAddress);
  }
  
  /**
   * Extracts the client IP from X-Forwarded-For or X-Real-IP headers.
   * 
   * @param request the HTTP request
   * @return the extracted IP or null if not found
   */
  private String extractFromForwardedHeader(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // X-Forwarded-For can contain multiple IPs, take the first one (client)
      String clientIp = xForwardedFor.split(",")[0].trim();
      if (isValidIpAddress(clientIp)) {
        return clientIp;
      }
    }
    
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty() && isValidIpAddress(xRealIp)) {
      return xRealIp;
    }
    
    return null;
  }
  
  /**
   * Basic validation to ensure the string looks like an IP address.
   * 
   * @param ipAddress the IP address to validate
   * @return true if valid format, false otherwise
   */
  private boolean isValidIpAddress(String ipAddress) {
    if (ipAddress == null || ipAddress.isEmpty()) {
      return false;
    }
    
    // Basic IPv4 pattern (simplified)
    if (ipAddress.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
      return true;
    }
    
    // Basic IPv6 pattern (simplified)
    if (ipAddress.contains(":") && ipAddress.matches("^[0-9a-fA-F:]+$")) {
      return true;
    }
    
    return false;
  }
}
