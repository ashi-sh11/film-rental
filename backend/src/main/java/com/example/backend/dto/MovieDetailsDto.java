package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

// Class-based DTO — keeps collections (cast & categories) which interface
// projections can't express cleanly when sourced from JOINed tables.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDetailsDto implements Serializable {

    private Integer filmId;

    private String title;

    private String description;

    private String releaseYear;

    private String language;

    private String rating;

    private Integer length;

    private Integer rentalDuration;

    private BigDecimal rentalRate;

    private BigDecimal replacementCost;

    private String specialFeatures;

    // Cast now carries actorId + name so the UI can link each name to the
    // actor's filmography page.
    private List<ActorSummary> actors;

    private List<String> categories;

    // Lightweight nested record — Jackson serializes it as {actorId, name}.
    public record ActorSummary(Integer actorId, String name) implements Serializable {}
}
