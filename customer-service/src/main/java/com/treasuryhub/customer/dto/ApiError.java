package com.treasuryhub.customer.dto;

import java.time.Instant;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error response")
public record ApiError(
        @Schema(description = "Time at which the error occurred", format = "date-time")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP status reason", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable error explanation", example = "Request validation failed")
        String message,

        @Schema(description = "Validation messages keyed by request field")
        Map<String, String> fieldErrors) {
}
