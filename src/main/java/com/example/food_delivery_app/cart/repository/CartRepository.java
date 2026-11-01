package com.example.food_delivery_app.cart.repository;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Hash per user:   key = cart:{userId}   field = menuItemId   value = quantity
 * Only talks to Redis. No business rules here.
 */
@Repository
public class CartRepository {


    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redis;

    // HashOperations<K, HK, HV>
    // K = key type (String)
    // HK = hash key type (String)
    // HV = hash value type (String)
    // e.g. for userId=123, menuItemId=456, quantity=2:
    private final HashOperations<String, String, String> hash;

    public CartRepository(StringRedisTemplate redis) {
        this.redis = redis;
        this.hash = redis.opsForHash();
    }

    private String key(Integer userId) {
        return "cart:" + userId;
    }

    // HINCRBY: atomic inside Redis, so two quick clicks never lose an update. Returns the new quantity.
    public long increment(Integer userId, Integer menuItemId, int delta) {
        Long newQuantity = hash.increment(key(userId), menuItemId.toString(), delta);
        redis.expire(key(userId), TTL);                       // every change renews the 7 days
        return newQuantity == null ? 0 : newQuantity;
    }

    public void setQuantity(Integer userId, Integer menuItemId, int quantity) {
        hash.put(key(userId), menuItemId.toString(), String.valueOf(quantity));
        redis.expire(key(userId), TTL);
    }

    public boolean contains(Integer userId, Integer menuItemId) {
        return hash.hasKey(key(userId), menuItemId.toString());
    }

    public boolean remove(Integer userId, Integer menuItemId) {
        Long removed = hash.delete(key(userId), menuItemId.toString());
        return removed != null && removed > 0;
    }

    public Map<Integer, Integer> findAll(Integer userId) {
        Map<Integer, Integer> cart = new HashMap<>();
        hash.entries(key(userId))
                .forEach((field, qty) -> cart.put(Integer.valueOf(field), Integer.valueOf(qty)));
        return cart;
    }

    public void clear(Integer userId) {
        redis.delete(key(userId));
    }
}

