package com.example.food_delivery_app.config;


import com.example.food_delivery_app.common.Constants;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching          // without it, @Cacheable / @CacheEvict are silently ignored
public class CacheConfig {


    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))          // default TTL for any cache
                .disableCachingNullValues();              // never cache "not found"

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withCacheConfiguration(Constants.MENU_CACHE, defaults.entryTtl(Duration.ofMinutes(10)))
                .build();
    }
}
