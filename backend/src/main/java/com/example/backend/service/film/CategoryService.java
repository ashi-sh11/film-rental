package com.example.backend.service.film;

import com.example.backend.dto.cache.CacheDtos.CategoryDto;
import com.example.backend.dto.projection.CategoryProjection;
import com.example.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories")
    public List<CategoryProjection> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .<CategoryProjection>map(CategoryDto::from)
                .toList();
    }
}
