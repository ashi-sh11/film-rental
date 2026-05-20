package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto implements Serializable {
    private Integer filmId;

    private String movieTitle;

    private Long totalCopies;

    private Long rentedCopies;

    private Long availableCopies;
}