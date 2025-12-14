package com.kshrd.reactiveredis.model.mapper;

import com.kshrd.reactiveredis.model.entity.Movie;
import com.kshrd.reactiveredis.model.response.MovieResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    MovieResponse toResponse(Movie movie);
}
