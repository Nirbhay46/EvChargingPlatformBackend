package io.evcharge.station.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis-backed cache for station read-paths.
 *
 * Notes:
 *  • TTL of 60 s on every entry (matches application.yml).
 *  • {@code GenericJackson2JsonRedisSerializer} needs polymorphic type info to
 *    deserialize back into {@code StationResponse} (a record / final class) —
 *    {@code DefaultTyping.EVERYTHING} adds an {@code @class} marker for finals too.
 *  • Validator allows only our own and JDK classes; prevents gadget deserialization.
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf) {
        var ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("io.evcharge")
                .allowIfSubType("java.")
                .build();

        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING);

        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "ev:station:" + cacheName + "::")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(om)));

        return RedisCacheManager.builder(cf).cacheDefaults(cfg).build();
    }
}
