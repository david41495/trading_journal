package com.davidserrano.tradejournal.auth;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.davidserrano.tradejournal.model.AppUser;
import com.davidserrano.tradejournal.model.Trade;
import com.davidserrano.tradejournal.repository.TradeRepository;
import com.davidserrano.tradejournal.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
            TradeRepository tradeRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tradeRepository = tradeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "An account with that email already exists.");
        }

        boolean firstAccount = userRepository.count() == 0;
        AppUser user = userRepository.save(new AppUser(
                request.displayName(), email,
                passwordEncoder.encode(request.password())));

        if (firstAccount) {
            List<Trade> legacyTrades = tradeRepository.findByOwnerIsNull();
            legacyTrades.forEach(trade -> trade.setOwner(user));
            tradeRepository.saveAll(legacyTrades);
        }

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AppUser requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
    }

    public AuthUserResponse toResponse(AppUser user) {
        return new AuthUserResponse(
                user.getId(), user.getDisplayName(), user.getEmail());
    }
}
