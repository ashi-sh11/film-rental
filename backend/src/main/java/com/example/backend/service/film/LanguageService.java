package com.example.backend.service.film;

import com.example.backend.dto.cache.CacheDtos.LanguageDto;
import com.example.backend.dto.projection.LanguageProjection;
import com.example.backend.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    @Cacheable(value = "languages")
    public List<LanguageProjection> getAllLanguages() {
        return languageRepository.findAllByOrderByNameAsc().stream()
                .<LanguageProjection>map(LanguageDto::from)
                .toList();
    }
}
