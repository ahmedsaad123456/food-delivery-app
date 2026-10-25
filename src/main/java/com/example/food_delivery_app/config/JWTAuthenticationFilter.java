package com.example.food_delivery_app.config;

import com.example.food_delivery_app.auth.service.TokenBlacklistService;
import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.handler.SecurityErrorHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * JWTAuthenticationFilter is a custom filter that intercepts incoming HTTP requests to validate JWT tokens.
 * It extends OncePerRequestFilter to ensure that the filter is executed only once per request.
 */
@RequiredArgsConstructor
@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    private final TokenBlacklistService blacklistService;
    private final SecurityErrorHandler securityErrorHandler;


    /*
    * This method is called for each incoming HTTP request.
    * It checks the request for a valid JWT token and performs authentication if the token is valid.
    *
    * */
    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(Constants.HEADER_AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT token from the Authorization header
        // 7 is the length of "Bearer "
        final String jwt = authHeader.substring(7);
    try{
        Claims claims = jwtService.parse(jwt);                       // signature + expiry checked here
        if (!blacklistService.isBlacklisted(claims.getId())          // logged-out token? reject
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Here you can implement your logic to authenticate the user based on the extracted username.
            // For example, you can load the user details from the database and set the authentication in the security context.
            UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

            if (userDetails.isEnabled() && jwtService.isTokenValid(claims, userDetails)) {

                    // create an authentication token using the user details and set it in the security context.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

                    // Set the details of the authentication token using the request information.
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // If the token is valid, create an authentication token and set it in the security context.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
        } catch (DataAccessResourceFailureException e) {
        // Redis (or the DB) is down: fail-closed with 503, do NOT continue the chain
        SecurityContextHolder.clearContext();
        securityErrorHandler.write(response, ErrorCode.SERVICE_UNAVAILABLE);
        return;
    }

        filterChain.doFilter(request, response);


    }
}
