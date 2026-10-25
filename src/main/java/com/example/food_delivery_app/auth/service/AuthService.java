package com.example.food_delivery_app.auth.service;

import com.example.food_delivery_app.auth.dto.AuthRequest;
import com.example.food_delivery_app.auth.dto.AuthResponse;
import com.example.food_delivery_app.auth.dto.RegisterRequest;
import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.config.JwtService;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.users.entity.Role;
import com.example.food_delivery_app.users.entity.RoleEntity;
import com.example.food_delivery_app.users.entity.User;
import com.example.food_delivery_app.users.repository.RoleRepository;
import com.example.food_delivery_app.users.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        RoleEntity customer = roleRepository.findByName(Role.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException(Constants.CUSTOMER_ERROR));

        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roleEntities(new HashSet<>(Set.of(customer)))     // mutable set, not Set.of(...) alone
                .enabled(true)
                .build();

        userRepository.save(user);
        return generateAccessTokenAndRefreshToken(user);
    }

    // The authenticate method is responsible for authenticating a user based on the provided email and password. It uses the AuthenticationManager to perform the authentication process.
    // If the authentication is successful, it retrieves the user from the database and generates a JWT token for the authenticated user.
    // if the authentication fails, it throws an exception indicating that the user was not found.
    public AuthResponse authenticate(AuthRequest request) {
        // The authenticate method is responsible for authenticating a user based on the provided email and password.
        // It uses the AuthenticationManager to perform the authentication process.
        // The AuthenticationManager.authenticate() method takes an instance of UsernamePasswordAuthenticationToken, which contains the user's email and password, and attempts to authenticate the user.
        // By using AuthenticationProvider (typically DaoAuthenticationProvider), it checks the provided credentials against the stored user details (e.g., in the database) and verifies if they match.
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        Object principal = auth.getPrincipal();

        if (!(principal instanceof User user)) {
            throw new IllegalStateException("Invalid authenticated principal");
        }

        return generateAccessTokenAndRefreshToken(user);// authenticate
    }

    // One place that creates both tokens, used by register and login
    private AuthResponse generateAccessTokenAndRefreshToken(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(refreshTokenService.createRefreshToken(user.getId()))   // starts a new family
                .build();
    }

    public AuthResponse getRefreshToken(String refreshToken) {
        // 1. Validate + consume the old token (throws 401 if invalid or reused)
        var rotated = refreshTokenService.rotate(refreshToken);

        // 2. Load the user fresh from the DB (the account may have been disabled meanwhile)
        var user = userRepository.findById(rotated.userId())
                .filter(User::isEnabled)
                .orElseThrow(() -> {
                    refreshTokenService.revoke(rotated.newToken());   // don't leave a live token for a dead account
                    return new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
                });

        // 3. New access token + the new refresh token we already created in rotate()
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(rotated.newToken())
                .build();
    }

    public void logout(String authHeader, String refreshToken) {
        // 1. Kill the refresh token (and its whole family): no more renewals
        refreshTokenService.revoke(refreshToken);

        // 2. Blacklist the current access token until it would have expired anyway
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(authHeader.substring(7));
                Duration remaining = Duration.between(Instant.now(), claims.getExpiration().toInstant());
                tokenBlacklistService.blacklist(claims.getId(), remaining);
            } catch (JwtException | IllegalArgumentException ignored) {
                // access token already expired or invalid: nothing to blacklist; logout still succeeds
            }
        }
    }


}
