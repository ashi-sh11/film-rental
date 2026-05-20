package com.example.backend.controller.film;

import com.example.backend.service.film.LocationService;
import com.example.backend.service.film.LocationService.CityDto;
import com.example.backend.service.film.LocationService.CountryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/countries")
    public List<CountryDto> getCountries() {
        return locationService.getCountries();
    }

    @GetMapping("/cities")
    public List<CityDto> getCities(@RequestParam Integer countryId) {
        return locationService.getCities(countryId);
    }
}
