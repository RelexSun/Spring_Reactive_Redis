package com.kshrd.reactiveredis.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Schema(description = "Standard API response wrapper")
public record APIResponse<T>(
        @Schema(description = "Response message", example = "Success")
        String message,

        @Schema(description = "HTTP status")
        HttpStatus status,

        @Schema(description = "Response payload")
        T payload,

        @Schema(description = "Response timestamp")
        Instant timestamp
) {}
