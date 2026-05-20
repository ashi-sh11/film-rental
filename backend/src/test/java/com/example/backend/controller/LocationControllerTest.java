package com.example.backend.controller;

import com.example.backend.controller.film.LocationController;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.security.CustomStaffDetailsService;
import com.example.backend.security.JwtService;
import com.example.backend.service.film.LocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = LocationController.class, excludeAutoConfiguration = {
        org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
        org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class LocationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomStaffDetailsService userDetailsService;
    @MockitoBean private LocationService locationService;

    @Test
    @DisplayName("GET /api/locations/countries — DTO list")
    void shouldListCountries() throws Exception {
        when(locationService.getCountries())
                .thenReturn(List.of(
                        new LocationService.CountryDto(1, "Afghanistan"),
                        new LocationService.CountryDto(2, "Algeria")));

        mockMvc.perform(get("/api/locations/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].countryId").value(1))
                .andExpect(jsonPath("$[0].name").value("Afghanistan"))
                .andExpect(jsonPath("$[1].name").value("Algeria"));
    }

    @Test
    @DisplayName("GET /api/locations/cities?countryId=44 — DTO list scoped to country")
    void shouldListCitiesForCountry() throws Exception {
        when(locationService.getCities(44))
                .thenReturn(List.of(
                        new LocationService.CityDto(8, "Adoni", 44),
                        new LocationService.CityDto(20, "Bhopal", 44)));

        mockMvc.perform(get("/api/locations/cities?countryId=44"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cityId").value(8))
                .andExpect(jsonPath("$[0].name").value("Adoni"))
                .andExpect(jsonPath("$[0].countryId").value(44));
    }

    @Test
    @DisplayName("GET /api/locations/cities — missing countryId → 400")
    void shouldRejectMissingCountryId() throws Exception {
        mockMvc.perform(get("/api/locations/cities"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("countryId")));
    }
}
