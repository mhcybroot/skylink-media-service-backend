package root.cyb.mh.skylink_media_service.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import root.cyb.mh.skylink_media_service.application.dto.api.LoginRequest;
import root.cyb.mh.skylink_media_service.application.dto.api.LoginResponse;
import root.cyb.mh.skylink_media_service.domain.entities.Admin;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;
import root.cyb.mh.skylink_media_service.infrastructure.security.jwt.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractorLoginUseCaseTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @InjectMocks
    private ContractorLoginUseCase contractorLoginUseCase;
    
    private Contractor testContractor;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        testContractor = new Contractor("contractor1", "$2a$10$encodedPassword", "John Doe");
        loginRequest = new LoginRequest("contractor1", "password123");
    }
    
    @Test
    void testSuccessfulLogin() {
        when(userRepository.findByUsername("contractor1")).thenReturn(Optional.of(testContractor));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(Contractor.class))).thenReturn("mock.jwt.token");
        when(jwtTokenProvider.getExpirationFromToken(anyString())).thenReturn(LocalDateTime.now().plusDays(1));
        when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(86400L);
        
        LoginResponse response = contractorLoginUseCase.execute(loginRequest);
        
        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("John Doe", response.getFullName());
        assertNotNull(response.getExpiresAt());
        assertEquals(86400L, response.getExpiresIn());
        
        verify(userRepository).findByUsername("contractor1");
        verify(passwordEncoder).matches("password123", "$2a$10$encodedPassword");
        verify(jwtTokenProvider).generateToken(testContractor);
    }
    
    @Test
    void testInvalidUsername() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        LoginRequest invalidRequest = new LoginRequest("nonexistent", "password123");
        
        assertThrows(BadCredentialsException.class, () -> {
            contractorLoginUseCase.execute(invalidRequest);
        });
        
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    void testInvalidPassword() {
        when(userRepository.findByUsername("contractor1")).thenReturn(Optional.of(testContractor));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPassword")).thenReturn(false);
        
        LoginRequest invalidRequest = new LoginRequest("contractor1", "wrongpassword");
        
        assertThrows(BadCredentialsException.class, () -> {
            contractorLoginUseCase.execute(invalidRequest);
        });
        
        verify(userRepository).findByUsername("contractor1");
        verify(passwordEncoder).matches("wrongpassword", "$2a$10$encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(any());
    }
    
    @Test
    void testNonContractorUser() {
        Admin admin = new Admin("admin1", "$2a$10$encodedPassword");
        when(userRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        
        LoginRequest adminRequest = new LoginRequest("admin1", "password123");
        
        assertThrows(BadCredentialsException.class, () -> {
            contractorLoginUseCase.execute(adminRequest);
        });
        
        verify(userRepository).findByUsername("admin1");
        verify(passwordEncoder).matches("password123", "$2a$10$encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}
