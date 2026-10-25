package com.example.food_delivery_app.auth.service;


import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final Duration TTL = Duration.ofDays(7);


    private final StringRedisTemplate redis;
    private final SecureRandom random = new SecureRandom();

    public record Rotated(int userId, String newToken) {}

    // New login = new family. Each device/session has its own family.
    public String createRefreshToken(int userId) {
        return generateToken(userId, UUID.randomUUID().toString());
    }

    // generate a new token in the same family. This is called "rotating" the refresh token.
    private String generateToken(int userId, String familyId) {
        byte[] bytes = new byte[32];                            // 32 bytes = 256 bits
        random.nextBytes(bytes); // randomly generate a new token
        // we store only the hash of the token in Redis, not the token itself, for security reasons
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = sha256(token);

        // Redis keeps only the hash, with a 7-day expiry (Redis deletes it automatically)
        redis.opsForValue().set(Constants.TOKEN_KEY + hash, userId + ":" + familyId, TTL);
        // Remember "the newest token of this family" so we can revoke it later
        redis.opsForValue().set(Constants.FAMILY_KEY + familyId, hash, TTL);

        return token;       // the RAW token goes to the client and is never stored
    }


    // Rotating a refresh token means that when the client uses a refresh token to get a new access token,
    // the server invalidates the old refresh token and generates a new one.
    // This is done to prevent replay attacks: if an attacker steals a refresh token, it will be useless after it has been used once.
    public Rotated rotate(String token) {
        String hash = sha256(token);

        // Read AND delete in one atomic step
        String value = redis.opsForValue().getAndDelete(Constants.TOKEN_KEY + hash);

        if (value == null) {
            // Not an active token. Was it used before?
            String familyId = redis.opsForValue().get(Constants.USED_KEY + hash);
            if (familyId != null) {
                revokeFamily(familyId);
            }
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String[] parts = value.split(":");
        int userId = Integer.parseInt(parts[0]);
        String familyId = parts[1];

        redis.opsForValue().set(Constants.USED_KEY + hash, familyId, TTL);    // remember it was consumed
        return new Rotated(userId, generateToken(userId, familyId));    // same family, new token
    }

    // Revoke a refresh token and its family.
    // This is called when the user logs out or when a refresh token is detected to be reused.
    public void revoke(String token) {
        String value = redis.opsForValue().getAndDelete(Constants.TOKEN_KEY + sha256(token));
        if (value != null) {
            revokeFamily(value.split(":")[1]);
        }
    }

    private void revokeFamily(String familyId) {
        String currentHash = redis.opsForValue().getAndDelete(Constants.FAMILY_KEY + familyId);
        if (currentHash != null) {
            redis.delete(Constants.TOKEN_KEY + currentHash);      // kill the newest token of the family
        }
    }


    // sha256() is a one-way hash function. It is used to store only the hash of the token in Redis, not the token itself, for security reasons.
    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }


}
