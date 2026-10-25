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
import java.util.UUID;

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


    // ":900000" is the default value (15 minutes) if the property is not defined in application.yml
    @Value("${jwt-access-expiration-ms:900000}")
    private long accessExpirationMs;



    // Turns the Base64 secret string into a real cryptographic key object.
    // Keys.hmacShaKeyFor throws an exception if the key is shorter than 256 bits.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    // Parses the token and returns ALL its claims (the payload).
    //  - verifyWith(key)       : checks the signature using our key
    //  - parseSignedClaims     : parses the token and rejects it if it is not signed
    // This method also validates the expiration: if the token is expired, or the signature
    // is wrong, or the token is malformed, it THROWS a JwtException (see note below).
    // Verifies the signature + expiration, then returns the payload.
    // Throws JwtException if anything is wrong (expired, tampered, malformed, unsigned).
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token) // parses the token and checks the signature and expiration
                .getPayload();
    }

    // Builds a signed token for a user, with optional extra claims (for example roles).
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        long now = System.currentTimeMillis();
        return Jwts.builder().claims(extraClaims)
                .id(UUID.randomUUID().toString())              // jti (JWT ID) claim: a unique identifier for this token, to prevent replay attacks
                .subject(userDetails.getUsername())            // the email
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();                                        // build the final "xxx.yyy.zzz" string
    }

    // Shortcut: generate a token with no extra claims.
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }



    // A token is valid when it belongs to this user AND has not expired.
    // (The signature is checked automatically while parsing the token.)
    // The parser already rejected expired tokens, so we only compare the subject here.
    public boolean isTokenValid(Claims claims, UserDetails userDetails) {
        return claims.getSubject().equals(userDetails.getUsername());
    }
}