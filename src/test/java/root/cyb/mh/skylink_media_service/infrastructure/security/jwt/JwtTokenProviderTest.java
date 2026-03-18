package root.cyb.mh.skylink_media_service.infrastructure.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
    
    private JwtTokenProvider jwtTokenProvider;
    private Contractor testContractor;
    
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);
        
        testContractor = new Contractor("testuser", "password123", "Test User");
        ReflectionTestUtils.setField(testContractor, "id", 1L);
    }
    
    @Test
    void testGenerateToken() {
        String token = jwtTokenProvider.generateToken(testContractor);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }
    
    @Test
    void testValidateToken() {
        String token = jwtTokenProvider.generateToken(testContractor);
        
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        assertTrue(isValid);
    }
    
    @Test
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        assertFalse(isValid);
    }
    
    @Test
    void testGetContractorIdFromToken() {
        String token = jwtTokenProvider.generateToken(testContractor);
        
        Long contractorId = jwtTokenProvider.getContractorIdFromToken(token);
        
        assertEquals(1L, contractorId);
    }
    
    @Test
    void testGetExpirationFromToken() {
        String token = jwtTokenProvider.generateToken(testContractor);
        
        LocalDateTime expiration = jwtTokenProvider.getExpirationFromToken(token);
        
        assertNotNull(expiration);
        assertTrue(expiration.isAfter(LocalDateTime.now()));
    }
    
    @Test
    void testGetExpirationInSeconds() {
        Long expirationInSeconds = jwtTokenProvider.getExpirationInSeconds();
        
        assertEquals(86400L, expirationInSeconds);
    }
}
