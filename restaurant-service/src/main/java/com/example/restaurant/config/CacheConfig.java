package com.example.restaurant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * Caching layer for frequently-read, computationally heavy restaurant data
 * (Part II §9). Ratings/top-rated are derived from reviews owned by
 * user-service, so cross-service invalidation isn't feasible — short TTLs
 * keep the cache reasonably fresh instead.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String ALL_RESTAURANTS_CACHE = "allRestaurants";
    public static final String TOP_RATED_CACHE = "topRated";
    public static final String RATINGS_CACHE = "ratings";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().build(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return builder -> builder.withInitialCacheConfigurations(Map.of(
                ALL_RESTAURANTS_CACHE, defaultConfig.entryTtl(Duration.ofSeconds(60)),
                TOP_RATED_CACHE, defaultConfig.entryTtl(Duration.ofSeconds(30)),
                RATINGS_CACHE, defaultConfig.entryTtl(Duration.ofSeconds(30))
        ));
    }
}
