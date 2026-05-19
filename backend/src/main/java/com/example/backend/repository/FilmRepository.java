package com.example.backend.repository;

import com.example.backend.dto.projection.FilmProjection;
import com.example.backend.entity.Film;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilmRepository extends JpaRepository<Film, Integer> {


    @EntityGraph(attributePaths = "language")
    Page<FilmProjection> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = "language")
    Page<FilmProjection> findAllProjectedBy(Pageable pageable);

    Page<Film> findDistinctByInventories_Store_StoreId(Integer storeId, Pageable pageable);

    Page<Film> findDistinctByInventories_Store_StoreIdAndTitleContainingIgnoreCase(
            Integer storeId, String title, Pageable pageable);

    @EntityGraph(attributePaths = "language")
    Page<FilmProjection> findDistinctByFilmActors_Actor_ActorId(Integer actorId, Pageable pageable);


    @EntityGraph(attributePaths = "language")
    Page<FilmProjection>
    findDistinctByFilmActors_Actor_FirstNameContainingIgnoreCaseAndFilmActors_Actor_LastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    @EntityGraph(attributePaths = "language")
    Page<FilmProjection>
    findDistinctByFilmActors_Actor_FirstNameContainingIgnoreCaseOrFilmActors_Actor_LastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    @EntityGraph(attributePaths = "language")
    Page<FilmProjection> findDistinctByFilmCategories_Category_NameIgnoreCase(
            String categoryName, Pageable pageable);

    @EntityGraph(attributePaths = {"language", "filmActors.actor"})
    java.util.Optional<Film> findDetailedByFilmId(Integer filmId);

}