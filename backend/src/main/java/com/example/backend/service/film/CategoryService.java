package com.example.backend.service.film;

import com.example.backend.dto.projection.CategoryProjection;
import com.example.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryProjection> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }
}