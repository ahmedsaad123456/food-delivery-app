package com.example.food_delivery_app.auth.service;

import com.example.food_delivery_app.auth.dto.AuthRequest;
import com.example.food_delivery_app.auth.dto.AuthResponse;
import com.example.food_delivery_app.auth.dto.RegisterRequest;
import com.example.food_delivery_app.config.JwtService;
import com.example.food_delivery_app.users.entity.Role;
import com.example.food_delivery_app.users.entity.User;
import com.example.food_delivery_app.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // Hash the password before storing it in the database
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .build();
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


        var user = (User) auth.getPrincipal();   // already loaded by userDetailsService, no second query


        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .build();
    }



}
