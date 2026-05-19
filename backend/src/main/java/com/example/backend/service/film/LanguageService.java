package com.example.backend.service.film;

import com.example.backend.dto.projection.LanguageProjection;
import com.example.backend.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    public List<LanguageProjection> getAllLanguages() {
        return languageRepository.findAllByOrderByNameAsc();
    }
}