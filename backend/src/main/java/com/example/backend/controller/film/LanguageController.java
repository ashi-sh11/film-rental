package com.example.backend.controller.film;

import com.example.backend.dto.projection.LanguageProjection;
import com.example.backend.service.film.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public List<LanguageProjection> getAllLanguages() {
        return languageService.getAllLanguages();
    }
}
