package com.example.food_delivery_app.auth.service;

import com.example.food_delivery_app.common.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redis;

    // add a JWT ID (jti) to the blacklist in Redis with a TTL equal to the remaining lifetime of the token.
    public void blacklist(String jti, Duration remaining) {
        if (jti != null && remaining.isPositive()) {          // already expired tokens don't need it
            redis.opsForValue().set(Constants.BLACK_LIST_KEY + jti, "1", remaining);
        }
    }

    // check if a JWT ID (jti) is in the blacklist in Redis. If it is, the token is considered invalid.
    public boolean isBlacklisted(String jti) {
        // hasKey() returns null if the key does not exist,
        // and true if it does. We return false if jti is null, because a null jti cannot be blacklisted.
        return jti != null && Boolean.TRUE.equals(redis.hasKey(Constants.BLACK_LIST_KEY + jti));
    }
}
