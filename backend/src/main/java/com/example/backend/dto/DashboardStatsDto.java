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
public class DashboardStatsDto implements Serializable {

    private Long totalCustomers;

    private Long totalMovies;

    private Long activeRentals;

    private Double totalRevenue;
}