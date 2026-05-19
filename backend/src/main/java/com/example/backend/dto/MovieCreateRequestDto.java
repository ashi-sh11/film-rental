package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

@Getter
@Setter
public class MovieCreateRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or fewer")
    private String title;

    @Size(max = 5000, message = "Description is too long")
    private String description;

    // Sakila stores YEAR. Accept anything starting with 4 digits (e.g. "2024", "2024-01-01").
    @Pattern(regexp = "^\\d{4}.*", message = "Release year must start with a 4-digit year")
    private String releaseYear;

    @JsonIgnore
    @AssertTrue(message = "Release year must be between 1888 and the current year")
    public boolean isReleaseYearWithinRange() {
        if (releaseYear == null || releaseYear.length() < 4) return true;
        try {
            int y = Integer.parseInt(releaseYear.substring(0, 4));
            return y >= 1888 && y <= Year.now().getValue();
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    @NotNull(message = "Language is required")
    @Positive(message = "Language ID must be positive")
    private Integer languageId;

    @Min(value = 1, message = "Rental duration must be at least 1 day")
    @Max(value = 30, message = "Rental duration cannot exceed 30 days")
    private Integer rentalDuration;

    @NotNull(message = "Rental rate is required")
    @DecimalMin(value = "0.00", message = "Rental rate cannot be negative")
    @Digits(integer = 2, fraction = 2, message = "Rental rate must be a valid amount (e.g. 4.99)")
    private BigDecimal rentalRate;

    @Min(value = 1, message = "Length must be a positive number of minutes")
    @Max(value = 1000, message = "Length is implausibly large")
    private Integer length;

    @DecimalMin(value = "0.00", message = "Replacement cost cannot be negative")
    @Digits(integer = 3, fraction = 2, message = "Replacement cost must be a valid amount")
    private BigDecimal replacementCost;

    // Sakila film.rating is an ENUM — reject anything outside the allowed set.
    @Pattern(regexp = "^(G|PG|PG-13|R|NC-17)$",
            message = "Rating must be one of G, PG, PG-13, R, NC-17")
    private String rating;

    @Size(max = 255, message = "Special features list is too long")
    private String specialFeatures;

    private List<@Positive(message = "Actor ID must be positive") Integer> actorIds;

    // New actors created on the fly during movie creation. Each is validated
    @Valid
    private List<NewActorDto> newActors;

    private List<@Positive(message = "Category ID must be positive") Integer> categoryIds;

    @Min(value = 1, message = "At least 1 copy must be added")
    @Max(value = 100, message = "Cannot add more than 100 copies at a time")
    private Integer copies;
}
