package com.vallexia.audit.unit.util;

import com.vallexia.audit.fixtures.AuditLogTestFixtures;
import com.vallexia.audit.util.IpAddressExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for IpAddressExtractor.
 * Tests IP extraction logic with various proxy configurations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@DisplayName("IpAddressExtractor Unit Tests")
class IpAddressExtractorTest {
  
  private static final String TRUSTED_PROXY = "10.0.0.1";
  private static final String UNTRUSTED_PROXY = "192.168.99.99";
  private static final String CLIENT_IP = "203.0.113.42";
  
  // ==================== extractClientIp() Tests ====================
  
  @Test
  @DisplayName("Should return remote address when no proxy is configured")
  void shouldReturnRemoteAddressWhenNoProxyIsConfigured() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor("");
    HttpServletRequest request = AuditLogTestFixtures.createMockRequest(
        CLIENT_IP, "Mozilla/5.0", "/test", "GET"
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then
    assertThat(result).isEqualTo(CLIENT_IP);
  }
  
  @Test
  @DisplayName("Should return forwarded IP from trusted proxy")
  void shouldReturnForwardedIpFromTrustedProxy() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor(TRUSTED_PROXY);
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        TRUSTED_PROXY,
        CLIENT_IP,
        null
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then
    assertThat(result).isEqualTo(CLIENT_IP);
  }
  
  @Test
  @DisplayName("Should return real IP from trusted proxy")
  void shouldReturnRealIpFromTrustedProxy() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor(TRUSTED_PROXY);
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        TRUSTED_PROXY,
        null,
        CLIENT_IP
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then
    assertThat(result).isEqualTo(CLIENT_IP);
  }
  
  @Test
  @DisplayName("Should ignore forwarded IP from untrusted proxy")
  void shouldIgnoreForwardedIpFromUntrustedProxy() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor(TRUSTED_PROXY);
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        UNTRUSTED_PROXY,
        CLIENT_IP,
        null
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then - should return proxy IP, not forwarded IP
    assertThat(result).isEqualTo(UNTRUSTED_PROXY);
  }
  
  @Test
  @DisplayName("Should return first IP when multiple IPs are forwarded")
  void shouldReturnFirstIpWhenMultipleIpsAreForwarded() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor(TRUSTED_PROXY);
    String multipleIps = CLIENT_IP + ", 10.0.0.2, 10.0.0.3";
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        TRUSTED_PROXY,
        multipleIps,
        null
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then
    assertThat(result).isEqualTo(CLIENT_IP);
  }
  
  @Test
  @DisplayName("Should return remote address when no trusted proxies are configured")
  void shouldReturnRemoteAddressWhenNoTrustedProxiesAreConfigured() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor("");
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        TRUSTED_PROXY,
        CLIENT_IP,
        null
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then - should not trust any proxy headers
    assertThat(result).isEqualTo(TRUSTED_PROXY);
  }
  
  // ==================== extractFullIpInfo() Tests ====================
  
  @Test
  @DisplayName("Should extract full IP info with proxy details")
  void shouldExtractFullIpInfoWithProxyDetails() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor(TRUSTED_PROXY);
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        TRUSTED_PROXY,
        CLIENT_IP,
        null
    );
    
    // When
    String result = extractor.extractFullIpInfo(request);
    
    // Then
    assertThat(result).contains(CLIENT_IP);
    assertThat(result).contains(TRUSTED_PROXY);
    assertThat(result).contains("X-Forwarded-For");
  }
  
  @Test
  @DisplayName("Should return simple remote address when no proxy")
  void shouldReturnSimpleRemoteAddressWhenNoProxy() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor("");
    HttpServletRequest request = AuditLogTestFixtures.createMockRequest(
        CLIENT_IP, "Mozilla/5.0", "/test", "GET"
    );
    
    // When
    String result = extractor.extractFullIpInfo(request);
    
    // Then
    assertThat(result).isEqualTo(CLIENT_IP);
    assertThat(result).doesNotContain("proxy");
  }
  
  // ==================== Invalid IP Handling Tests ====================
  
  @Test
  @DisplayName("Should return remote address when forwarded IP is invalid")
  void shouldReturnRemoteAddressWhenForwardedIpIsInvalid() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor(TRUSTED_PROXY);
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        TRUSTED_PROXY,
        "invalid-ip",
        null
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then - should fall back to remote address
    assertThat(result).isEqualTo(TRUSTED_PROXY);
  }
  
  // ==================== IPv6 Support Tests ====================
  
  @Test
  @DisplayName("Should handle IPv6 address correctly")
  void shouldHandleIpv6AddressCorrectly() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor(TRUSTED_PROXY);
    String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        TRUSTED_PROXY,
        ipv6,
        null
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then
    assertThat(result).isEqualTo(ipv6);
  }
  
  // ==================== Constructor Tests ====================
  
  @Test
  @DisplayName("Should parse multiple trusted proxies from configuration")
  void shouldParseMultipleTrustedProxiesFromConfiguration() {
    // Given & When
    IpAddressExtractor extractor = new IpAddressExtractor(
        "10.0.0.1, 10.0.0.2 , 10.0.0.3"
    );
    
    // Then - test by using the extractor
    HttpServletRequest request1 = AuditLogTestFixtures.createMockRequestWithProxy(
        "10.0.0.1",
        CLIENT_IP,
        null
    );
    HttpServletRequest request2 = AuditLogTestFixtures.createMockRequestWithProxy(
        "10.0.0.2",
        CLIENT_IP,
        null
    );
    
    assertThat(extractor.extractClientIp(request1)).isEqualTo(CLIENT_IP);
    assertThat(extractor.extractClientIp(request2)).isEqualTo(CLIENT_IP);
  }
  
  @Test
  @DisplayName("Should handle null trusted proxies configuration")
  void shouldHandleNullTrustedProxiesConfiguration() {
    // Given & When
    IpAddressExtractor extractor = new IpAddressExtractor(null);
    
    // Then - should work without errors
    HttpServletRequest request = AuditLogTestFixtures.createMockRequest();
    assertThat(extractor.extractClientIp(request)).isNotNull();
  }
  
  // ==================== Header Priority Tests ====================
  
  @Test
  @DisplayName("Should prefer X-Real-IP over remote address")
  void shouldPreferXRealIpOverRemoteAddress() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor(TRUSTED_PROXY);
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        TRUSTED_PROXY,
        null,
        CLIENT_IP
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then
    assertThat(result).isEqualTo(CLIENT_IP);
  }
  
  @Test
  @DisplayName("Should prefer X-Forwarded-For over X-Real-IP")
  void shouldPreferXForwardedForOverXRealIp() {
    // Given
    IpAddressExtractor extractor = new IpAddressExtractor(TRUSTED_PROXY);
    String forwardedIp = "203.0.113.50";
    HttpServletRequest request = AuditLogTestFixtures.createMockRequestWithProxy(
        TRUSTED_PROXY,
        forwardedIp,
        CLIENT_IP
    );
    
    // When
    String result = extractor.extractClientIp(request);
    
    // Then - X-Forwarded-For should take precedence
    assertThat(result).isEqualTo(forwardedIp);
  }
}
