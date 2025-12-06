package com.vallexia.security.util;

import com.vallexia.config.security.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for securely extracting client IP addresses from HTTP requests.
 * Handles proxy headers with validation against trusted proxy list.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Slf4j
@Component
public class IpAddressExtractor {
  
  private final Set<String> trustedProxies;
  
  /**
   * Constructor with configurable trusted proxies.
   * 
   * @param securityProperties security configuration properties
   */
  public IpAddressExtractor(SecurityProperties securityProperties) {
    this.trustedProxies = new HashSet<>();
    
    String trustedProxiesConfig = securityProperties.getTrustedProxies();
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
        String forwardedIp = extractFromForwardedHeader(request);
        if (forwardedIp != null) {
          return String.format("%s (via proxy %s, X-Forwarded-For: %s)", 
              forwardedIp, remoteAddr, xForwardedFor);
        }
      } else if (xRealIp != null && !xRealIp.isEmpty()) {
        if (isValidIpAddress(xRealIp)) {
          return String.format("%s (via proxy %s, X-Real-IP: %s)", 
              xRealIp, remoteAddr, xRealIp);
        }
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
   * Validates if the given string is a valid IP address (IPv4 or IPv6).
   * Uses java.net.InetAddress for robust validation that handles all IPv4 and IPv6 formats,
   * including compressed IPv6, mixed notation, and edge cases.
   * 
   * This method prevents DNS lookups by checking the format first, then using InetAddress
   * only for numeric IP addresses to validate the range and format correctness.
   * 
   * @param ipAddress the IP address to validate
   * @return true if valid format, false otherwise
   */
  private boolean isValidIpAddress(String ipAddress) {
    if (ipAddress == null || ipAddress.trim().isEmpty()) {
      return false;
    }
    
    String trimmed = ipAddress.trim();
    
    // Quick check: IP addresses should only contain numeric characters, dots, colons, and brackets
    // This prevents DNS lookups for hostnames
    if (!trimmed.matches("^[0-9a-fA-F:.\\[\\]]+$")) {
      return false;
    }
    
    // Remove IPv6 brackets if present for validation
    String addressToValidate = trimmed.replaceAll("^\\[|\\]$", "");
    
    try {
      // Use InetAddress.getByName() to validate the IP address format and range
      // Since we've filtered out hostnames above, this will only parse IP addresses
      // This validates:
      // - IPv4: correct octet ranges (0-255), proper format
      // - IPv6: correct hex format, compressed notation (::), mixed notation
      // - Rejects invalid ranges (e.g., 256.256.256.256)
      InetAddress addr = InetAddress.getByName(addressToValidate);
      
      // Verify it's actually an IP address and not a hostname
      // getHostAddress() returns the numeric IP, so if it matches patterns, it's an IP
      String hostAddress = addr.getHostAddress();
      
      // For IPv4, check that getHostAddress() is in IPv4 format
      // For IPv6, check that it's in IPv6 format (contains colons)
      // This ensures we didn't accidentally accept a hostname
      boolean isIPv4 = hostAddress.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
      boolean isIPv6 = hostAddress.contains(":");
      
      if (!isIPv4 && !isIPv6) {
        // Not a valid IP format
        return false;
      }
      
      // Additional validation: ensure the byte array length matches expected IP version
      byte[] addressBytes = addr.getAddress();
      if (isIPv4 && addressBytes.length != 4) {
        return false;
      }
      if (isIPv6 && addressBytes.length != 16) {
        return false;
      }
      
      // Successfully validated as IP address
      return true;
             
    } catch (UnknownHostException e) {
      // getByName() couldn't parse it as an IP address
      log.debug("Invalid IP address format: {}", trimmed);
      return false;
    } catch (Exception e) {
      // Catch any other exceptions (security or parsing issues)
      log.debug("Error validating IP address {}: {}", trimmed, e.getClass().getSimpleName());
      return false;
    }
  }
}
