package com.example.backend.controller.film;

import com.example.backend.dto.projection.CategoryProjection;
import com.example.backend.service.film.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryProjection> getAllCategories() {
        return categoryService.getAllCategories();
    }
}