package com.kshrd.reactiveredis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kshrd.reactiveredis.model.response.MovieResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * ReactiveRedisTemplate for storing JSON strings
     * This is the most flexible approach for caching
     */
    @Primary
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisSerializationContext<String, String> serializationContext =
                RedisSerializationContext
                        .<String, String>newSerializationContext(stringSerializer)
                        .key(stringSerializer)
                        .value(stringSerializer)
                        .hashKey(stringSerializer)
                        .hashValue(stringSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    /**
     * Alternative: ReactiveRedisTemplate for storing MovieResponse objects directly
     * Useful if you want Redis to handle serialization automatically
     */
    @Bean(name = "movieRedisTemplate")
    public <T> ReactiveRedisTemplate<String, MovieResponse> redisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        JacksonJsonRedisSerializer<MovieResponse> serializer =
                new JacksonJsonRedisSerializer<>(MovieResponse.class);

        RedisSerializationContext<String, MovieResponse> context =
                RedisSerializationContext
                        .<String, MovieResponse>newSerializationContext(RedisSerializer.string())
                        .value(serializer)
                        .hashValue(serializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
