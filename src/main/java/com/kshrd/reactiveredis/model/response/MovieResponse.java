package com.kshrd.reactiveredis.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

    private Long id;

    private String title;

    private String description;

    private Integer releaseYear;

    private String genre;

    private String director;

    private BigDecimal rating;

    private Integer durationMinutes;

    private List<ReviewResponse> reviews;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
