package com.davidserrano.tradejournal.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.davidserrano.tradejournal.model.AppUser;
import com.davidserrano.tradejournal.model.Trade;
import com.davidserrano.tradejournal.repository.TradeRepository;
import com.davidserrano.tradejournal.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TradeRepository tradeRepository;

    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4);
        authService = new AuthService(
                userRepository, tradeRepository, passwordEncoder);
    }

    @Test
    void registerHashesPasswordAndClaimsLegacyTradesForFirstAccount() {
        Trade legacyTrade = new Trade();
        when(userRepository.existsByEmailIgnoreCase("david@example.com"))
                .thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tradeRepository.findByOwnerIsNull())
                .thenReturn(List.of(legacyTrade));

        authService.register(new RegisterRequest(
                "David", "DAVID@example.com", "secure-pass-123"));

        verify(tradeRepository).saveAll(List.of(legacyTrade));
        assertThat(legacyTrade.getOwner()).isNotNull();
        assertThat(legacyTrade.getOwner().getPasswordHash())
                .isNotEqualTo("secure-pass-123");
        assertThat(passwordEncoder.matches(
                "secure-pass-123", legacyTrade.getOwner().getPasswordHash()))
                .isTrue();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("david@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "David", "david@example.com", "secure-pass-123")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }
}
