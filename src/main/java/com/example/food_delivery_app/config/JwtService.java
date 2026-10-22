package com.example.food_delivery_app.config;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Creates and reads JWT tokens.
 * A JWT is three Base64 parts joined by dots:  header.payload.signature
 *   - header    : the algorithm used (HS256 here)
 *   - payload   : the "claims" (username, issued-at, expiration, ...). It is only
 *                 encoded, NOT encrypted, so anyone can read it. Never put passwords in it.
 *   - signature : created with our secret key. If anyone changes the payload,
 *                 the signature no longer matches and the token is rejected.
 */
@Service
public class JwtService {

    // The secret used to sign tokens. It is read from application.yml (key: jwt-secret-key),
    // and it must be a Base64 string that decodes to at least 32 bytes (256 bits) for HS256.
    // Keep it out of git: set it from an environment variable instead.
    @Value("${jwt-secret-key}")
    private String secretKey;

    // Reads the username from the token. In our tokens the username is stored
    // in the standard "subject" (sub) claim.
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Turns the Base64 secret string into a real cryptographic key object.
    // Keys.hmacShaKeyFor throws an exception if the key is shorter than 256 bits.
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Parses the token and returns ALL its claims (the payload).
    //  - verifyWith(key)       : checks the signature using our key
    //  - parseSignedClaims     : parses the token and rejects it if it is not signed
    // This method also validates the expiration: if the token is expired, or the signature
    // is wrong, or the token is malformed, it THROWS a JwtException (see note below).
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Generic helper: extract any single claim from the token.
    // The caller says which claim it wants by passing a function:
    //   extractClaim(token, Claims::getSubject)     -> username
    //   extractClaim(token, Claims::getExpiration)  -> expiration date
    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    // Builds a signed token for a user, with optional extra claims (for example roles).
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(extraClaims)                                  // custom data
                .subject(userDetails.getUsername())                   // who this token belongs to
                .issuedAt(new Date(now))                              // when it was created
                .expiration(new Date(now + 1000L * 60 * 60 * 24))     // 24 hours from now (in milliseconds)
                .signWith(getSigningKey(), Jwts.SIG.HS256)            // sign with our key using HS256
                .compact();                                           // build the final "xxx.yyy.zzz" string
    }

    // Reads the expiration date from the token.
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Shortcut: generate a token with no extra claims.
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // true if the expiration date is in the past.
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // A token is valid when it belongs to this user AND has not expired.
    // (The signature is checked automatically while parsing the token.)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}