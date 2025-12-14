package com.kshrd.reactiveredis.model.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.reactiveredis.exceptions.ConversionException;
import com.kshrd.reactiveredis.model.entity.Movie;
import com.kshrd.reactiveredis.model.response.MovieResponse;
import org.springframework.stereotype.Component;

@Component
public class MovieJsonMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(Movie movie) throws ConversionException {
        try {
            return objectMapper.writeValueAsString(movie);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Error converting Movie to JSON");
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) throws ConversionException {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Error converting JSON to Movie");
        }
    }
}

