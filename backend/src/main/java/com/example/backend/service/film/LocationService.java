package com.example.backend.service.film;

import com.example.backend.repository.CityRepository;
import com.example.backend.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    public record CountryDto(Integer countryId, String name) implements Serializable {}

    public record CityDto(Integer cityId, String name, Integer countryId) implements Serializable {}

    @Cacheable("countries")
    public List<CountryDto> getCountries() {
        return countryRepository.findAllByOrderByCountryAsc().stream()
                .map(p -> new CountryDto(p.getCountryId(), p.getName()))
                .toList();
    }

    @Cacheable(value = "cities", key = "#countryId")
    public List<CityDto> getCities(Integer countryId) {
        return cityRepository.findByCountry_CountryIdOrderByCityAsc(countryId).stream()
                .map(p -> new CityDto(p.getCityId(), p.getName(), p.getCountryId()))
                .toList();
    }
}
