package com.example.backend.service.film;

import com.example.backend.dto.*;
import com.example.backend.dto.cache.CacheDtos.CategoryDto;
import com.example.backend.dto.cache.CacheDtos.FilmDto;
import com.example.backend.dto.cache.CacheDtos.LanguageDto;
import com.example.backend.dto.projection.CategoryProjection;
import com.example.backend.dto.projection.FilmProjection;
import com.example.backend.dto.projection.LanguageProjection;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.*;
import com.example.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilmService {

    private final FilmRepository filmRepository;
    private final FilmActorRepository filmActorRepository;
    private final FilmCategoryRepository filmCategoryRepository;
    private final InventoryRepository inventoryRepository;
    private final StaffRepository staffRepository;
    private final LanguageRepository languageRepository;
    private final CategoryRepository categoryRepository;
    private final ActorRepository actorRepository;
    private final AuthUtil authUtil;
    private final StoreRepository storeRepository;


    @Cacheable(value = "movies", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<FilmProjection> getAllMovies(Pageable pageable) {
        return filmRepository.findAllProjectedBy(pageable)
                .map(p -> (FilmProjection) FilmDto.from(p));
    }

    @Cacheable(value = "movieSearch",
            key = "#title + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<FilmProjection> searchMovies(String title, Pageable pageable) {
        return filmRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(p -> (FilmProjection) FilmDto.from(p));
    }


    @Cacheable(value = "moviesByActor",
            key = "#actorName + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<FilmProjection> searchMoviesByActor(String actorName, Pageable pageable) {
        String trimmed = actorName.trim();
        String[] parts = trimmed.split("\\s+");

        Page<FilmProjection> raw = parts.length >= 2
                ? filmRepository
                        .findDistinctByFilmActors_Actor_FirstNameContainingIgnoreCaseAndFilmActors_Actor_LastNameContainingIgnoreCase(
                                parts[0], parts[parts.length - 1], pageable)
                : filmRepository
                        .findDistinctByFilmActors_Actor_FirstNameContainingIgnoreCaseOrFilmActors_Actor_LastNameContainingIgnoreCase(
                                trimmed, trimmed, pageable);
        return raw.map(p -> (FilmProjection) FilmDto.from(p));
    }

    @Cacheable(value = "moviesByCategory",
            key = "#categoryName + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<FilmProjection> getMoviesByCategory(String categoryName, Pageable pageable) {
        return filmRepository.findDistinctByFilmCategories_Category_NameIgnoreCase(categoryName, pageable)
                .map(p -> (FilmProjection) FilmDto.from(p));
    }

    // ===== Languages / Categories (interface projections) =====

    @Cacheable(value = "languages")
    public List<LanguageProjection> getAllLanguages() {
        return languageRepository.findAllByOrderByNameAsc().stream()
                .<LanguageProjection>map(LanguageDto::from)
                .toList();
    }

    @Cacheable(value = "categories")
    public List<CategoryProjection> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .<CategoryProjection>map(CategoryDto::from)
                .toList();
    }


    @Cacheable(value = "movieDetails", key = "#filmId")
    public MovieDetailsDto getMovieDetails(Integer filmId) {
        Film film = filmRepository.findDetailedByFilmId(filmId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        List<MovieDetailsDto.ActorSummary> actors = film.getFilmActors().stream()
                .map(fa -> new MovieDetailsDto.ActorSummary(
                        fa.getActor().getActorId(),
                        fa.getActor().getFirstName() + " " + fa.getActor().getLastName()))
                .toList();

        List<String> categories = film.getFilmCategories().stream()
                .map(fc -> fc.getCategory().getName())
                .toList();

        return MovieDetailsDto.builder()
                .filmId(film.getFilmId())
                .title(film.getTitle())
                .description(film.getDescription())
                .releaseYear(String.valueOf(film.getReleaseYear()))
                .language(film.getLanguage().getName())
                .rating(film.getRating())
                .length(film.getLength())
                .rentalDuration(film.getRentalDuration())
                .rentalRate(film.getRentalRate())
                .replacementCost(film.getReplacementCost())
                .specialFeatures(film.getSpecialFeatures())
                .actors(actors)
                .categories(categories)
                .build();
    }

    private void linkActorToFilm(Actor actor, Film film) {
        FilmActorId id = new FilmActorId();
        id.setActorId(actor.getActorId());
        id.setFilmId(film.getFilmId());
        FilmActor fa = new FilmActor();
        fa.setId(id);
        fa.setActor(actor);
        fa.setFilm(film);
        fa.setLastUpdate(LocalDateTime.now());
        filmActorRepository.save(fa);
    }

    @Caching(evict = {
            @CacheEvict(value = "dashboardStats",  allEntries = true),
            @CacheEvict(value = "movieDetails",    allEntries = true),
            @CacheEvict(value = "movies",          allEntries = true),
            @CacheEvict(value = "movieSearch",     allEntries = true),
            @CacheEvict(value = "moviesByActor",   allEntries = true),
            @CacheEvict(value = "moviesByCategory", allEntries = true),
            @CacheEvict(value = "actors",          allEntries = true),
            @CacheEvict(value = "actorsBasic",     allEntries = true),
            @CacheEvict(value = "actorSearch",     allEntries = true),
            @CacheEvict(value = "actorById",       allEntries = true),
            @CacheEvict(value = "actorMovies",     allEntries = true),
            @CacheEvict(value = "inventory",       allEntries = true),
            @CacheEvict(value = "storeInventory",  allEntries = true)
    })
    @Transactional
    public Integer createMovie(MovieCreateRequestDto request) {
        String username = authUtil.getLoggedInUsername();
        Staff staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found"));

        Film film = new Film();
        film.setTitle(request.getTitle());
        film.setDescription(request.getDescription());
        film.setReleaseYear(Integer.valueOf(request.getReleaseYear()));
        film.setLanguage(language);
        film.setRentalDuration(request.getRentalDuration() != null ? request.getRentalDuration() : 3);
        film.setRentalRate(request.getRentalRate());
        film.setLength(request.getLength());
        film.setReplacementCost(request.getReplacementCost());
        film.setRating(request.getRating());
        film.setSpecialFeatures(request.getSpecialFeatures());
        film.setLastUpdate(LocalDateTime.now());
        Film saved = filmRepository.save(film);

        // Link existing actors selected from the search dropdown.
        if (request.getActorIds() != null) {
            for (Integer actorId : request.getActorIds()) {
                Actor actor = actorRepository.findById(actorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Actor not found: " + actorId));
                linkActorToFilm(actor, saved);
            }
        }

        // Create + link brand-new actors entered inline in the form.
        if (request.getNewActors() != null && !request.getNewActors().isEmpty()) {
            // Pre-compute next actor id once; we increment locally as we save more.
            int nextActorId = actorRepository.findTopByOrderByActorIdDesc()
                    .map(a -> a.getActorId() + 1)
                    .orElse(1);

            for (NewActorDto na : request.getNewActors()) {
                Actor newActor = new Actor();
                newActor.setActorId(nextActorId++);
                newActor.setFirstName(na.getFirstName().trim());
                newActor.setLastName(na.getLastName().trim());
                newActor.setLastUpdate(LocalDateTime.now());
                Actor savedActor = actorRepository.save(newActor);

                linkActorToFilm(savedActor, saved);
            }
        }

        if (request.getCategoryIds() != null) {
            for (Integer categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
                FilmCategoryId id = new FilmCategoryId();
                id.setFilmId(saved.getFilmId());
                id.setCategoryId(categoryId);
                FilmCategory fc = new FilmCategory();
                fc.setId(id);
                fc.setFilm(saved);
                fc.setCategory(category);
                fc.setLastUpdate(LocalDateTime.now());
                filmCategoryRepository.save(fc);
            }
        }
        Integer storeId = staff.getStoreId();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + storeId));

        int copies = (request.getCopies() != null && request.getCopies() > 0) ? request.getCopies() : 1;
        for (int i = 0; i < copies; i++) {
            Inventory inv = new Inventory();
            inv.setFilm(saved);
            inv.setStore(store);
            inv.setLastUpdate(LocalDateTime.now());
            inventoryRepository.save(inv);
        }

        return saved.getFilmId();
    }
}
