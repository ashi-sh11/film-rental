package com.example.backend.service;

import com.example.backend.dto.projection.ActorProjection;
import com.example.backend.dto.projection.FilmProjection;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.ActorRepository;
import com.example.backend.repository.FilmRepository;
import com.example.backend.service.film.ActorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActorServiceTest {

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private FilmRepository filmRepository;

    @InjectMocks
    private ActorService actorService;


    private ActorProjection actorProjection(Integer id, String first, String last) {
        return new ActorProjection() {
            @Override public Integer getActorId()     { return id; }
            @Override public String  getActorName()   { return first + " " + last; }
            @Override public Long    getTotalMovies() { return 0L; }
        };
    }

    private FilmProjection filmProjection(Integer id, String title) {
        return new FilmProjection() {
            @Override public Integer    getFilmId()     { return id; }
            @Override public String     getTitle()      { return title; }
            @Override public String     getReleaseYear(){ return "2024"; }
            @Override public String     getLanguage()   { return "English"; }
            @Override public BigDecimal getRentalRate() { return BigDecimal.valueOf(2.99); }
            @Override public String     getRating()     { return "PG"; }
            @Override public Integer    getLength()     { return 120; }
        };
    }

    @Test
    @DisplayName("getAllActors — should return paged actor projections")
    void shouldReturnPagedActors() {
        Page<ActorProjection> page = new PageImpl<>(
                List.of(actorProjection(1, "Tom", "Hanks")));

        when(actorRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(page);

        Page<ActorProjection> result = actorService.getAllActors(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getActorName()).isEqualTo("Tom Hanks");
    }

    @Test
    @DisplayName("getAllActors — should return empty page when no actors exist")
    void shouldReturnEmptyPagedActors() {
        when(actorRepository.findAllProjectedBy(any(PageRequest.class)))
                .thenReturn(Page.empty());

        Page<ActorProjection> result = actorService.getAllActors(PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getAllActorsBasic — should return flat list sorted by name")
    void shouldReturnAllActorsBasic() {
        List<ActorProjection> actors = List.of(
                actorProjection(1, "Alice", "Brown"),
                actorProjection(2, "Bob",   "Smith"));

        when(actorRepository.findAllByOrderByFirstNameAscLastNameAsc()).thenReturn(actors);

        List<ActorProjection> result = actorService.getAllActorsBasic();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getActorName()).isEqualTo("Alice Brown");
        assertThat(result.get(1).getActorName()).isEqualTo("Bob Smith");
    }

    @Test
    @DisplayName("getAllActorsBasic — should return empty list when none exist")
    void shouldReturnEmptyBasicList() {
        when(actorRepository.findAllByOrderByFirstNameAscLastNameAsc()).thenReturn(List.of());

        assertThat(actorService.getAllActorsBasic()).isEmpty();
    }

    @Test
    @DisplayName("searchActor — single word uses OR query on first/last name")
    void shouldSearchActorBySingleWord() {
        Page<ActorProjection> page = new PageImpl<>(
                List.of(actorProjection(1, "Tom", "Hanks")));

        when(actorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                eq("tom"), eq("tom"), any(PageRequest.class)))
                .thenReturn(page);

        Page<ActorProjection> result =
                actorService.searchActor("tom", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getActorName()).isEqualTo("Tom Hanks");
    }

    @Test
    @DisplayName("searchActor — two words uses AND query on first + last name")
    void shouldSearchActorByFullName() {
        Page<ActorProjection> page = new PageImpl<>(
                List.of(actorProjection(1, "Tom", "Hanks")));

        when(actorRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                eq("Tom"), eq("Hanks"), any(PageRequest.class)))
                .thenReturn(page);

        Page<ActorProjection> result =
                actorService.searchActor("Tom Hanks", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("searchActor — leading/trailing whitespace is trimmed before splitting")
    void shouldTrimWhitespaceBeforeSearch() {
        Page<ActorProjection> page = new PageImpl<>(
                List.of(actorProjection(1, "Tom", "Hanks")));

        when(actorRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                eq("Tom"), eq("Hanks"), any(PageRequest.class)))
                .thenReturn(page);

        Page<ActorProjection> result =
                actorService.searchActor("  Tom Hanks  ", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("searchActor — returns empty when no match found")
    void shouldReturnEmptyPageForNoMatch() {
        when(actorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                eq("xyz"), eq("xyz"), any(PageRequest.class)))
                .thenReturn(Page.empty());

        Page<ActorProjection> result =
                actorService.searchActor("xyz", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getActorById — should return projection when actor exists")
    void shouldReturnActorById() {
        ActorProjection proj = actorProjection(5, "Meryl", "Streep");
        when(actorRepository.findProjectedByActorId(5)).thenReturn(Optional.of(proj));

        ActorProjection result = actorService.getActorById(5);

        assertThat(result.getActorId()).isEqualTo(5);
        assertThat(result.getActorName()).isEqualTo("Meryl Streep");
    }

    @Test
    @DisplayName("getActorById — should throw ResourceNotFoundException when actor not found")
    void shouldThrowWhenActorNotFound() {
        when(actorRepository.findProjectedByActorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actorService.getActorById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Actor not found");
    }

    @Test
    @DisplayName("getActorMovies — should return paged films for valid actor")
    void shouldReturnMoviesForActor() {
        ActorProjection proj = actorProjection(1, "Tom", "Hanks");
        when(actorRepository.existsById(1)).thenReturn(true);

        Page<FilmProjection> films = new PageImpl<>(
                List.of(filmProjection(10, "Forrest Gump")));
        when(filmRepository.findDistinctByFilmActors_Actor_ActorId(eq(1), any(PageRequest.class)))
                .thenReturn(films);

        Page<FilmProjection> result =
                actorService.getActorMovies(1, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Forrest Gump");
    }

    @Test
    @DisplayName("getActorMovies — should throw ResourceNotFoundException when actor does not exist")
    void shouldThrowWhenActorNotFoundForMovies() {
        when(actorRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> actorService.getActorMovies(99, PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Actor not found");
    }

    @Test
    @DisplayName("getActorMovies — should return empty page when actor has no films")
    void shouldReturnEmptyPageWhenActorHasNoMovies() {
        ActorProjection proj = actorProjection(2, "New", "Actor");
        when(actorRepository.existsById(2)).thenReturn(true);
        when(filmRepository.findDistinctByFilmActors_Actor_ActorId(eq(2), any(PageRequest.class)))
                .thenReturn(Page.empty());

        Page<FilmProjection> result =
                actorService.getActorMovies(2, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }
}