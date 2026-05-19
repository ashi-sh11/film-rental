package com.example.backend.repository;

import com.example.backend.entity.Film;
import com.example.backend.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Long countByFilm(Film film);

    // Count inventory items for a specific film by filmId
    Long countByFilm_FilmId(Integer filmId);

    // Count inventory items for a specific film at a specific store
    Long countByFilm_FilmIdAndStore_StoreId(Integer filmId, Integer storeId);

    // Get all inventory items for a store
    @EntityGraph(attributePaths = {"film", "store"})
    List<Inventory> findByStore_StoreId(Integer storeId);

    // Get distinct films in inventory for a store (paginated)
    @EntityGraph(attributePaths = {"film", "store"})
    Page<Inventory> findByStore_StoreId(Integer storeId, Pageable pageable);

    // Batch fetch inventory rows for many films in one query (avoids N+1 counts)
    @EntityGraph(attributePaths = {"film", "store"})
    List<Inventory> findByStore_StoreIdAndFilm_FilmIdIn(Integer storeId, List<Integer> filmIds);
}
