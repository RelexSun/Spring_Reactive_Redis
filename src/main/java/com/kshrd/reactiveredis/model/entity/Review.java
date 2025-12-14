package com.kshrd.reactiveredis.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("reviews")
public class Review implements Serializable {

    @Id
    private Long id;

    @Column("movie_id")
    private Long movieId;

    @Column("reviewer_name")
    private String reviewerName;

    @Column("rating")
    private Integer rating;

    @Column("comment")
    private String comment;

    @Column("created_at")
    private LocalDateTime createdAt;
}
