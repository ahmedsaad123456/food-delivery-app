package com.example.food_delivery_app.config;

import com.example.food_delivery_app.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class ApplicationConfig {

    private final UserRepository userRepository;


    // UserDetailsService is an interface that defines a contract for retrieving user details.
    // It is responsible for loading user-specific data during authentication.
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmailWithAuthorities(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email " + email
                        )
                );
    }


    // PasswordEncoder is an interface that defines a contract for encoding passwords.
    // The BCryptPasswordEncoder is a concrete implementation of this interface that uses the BCrypt hashing algorithm to hash passwords and verify them.

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // AuthenticationProvider is an interface that defines a contract for authentication. It is responsible for authenticating a user based on the provided credentials.
    // The DaoAuthenticationProvider is a concrete implementation of this interface that uses a UserDetailsService to retrieve user details and a PasswordEncoder to validate passwords.
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        // DaoAuthenticationProvider is an implementation of AuthenticationProvider that uses a UserDetailsService to retrieve user details and a PasswordEncoder to validate passwords.

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }


    // AuthenticationManager is an interface that defines a contract for managing authentication.
    // It is responsible for processing authentication requests and returning an authenticated user if the credentials are valid.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

}