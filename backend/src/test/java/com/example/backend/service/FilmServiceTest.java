package com.example.backend.service;

import com.example.backend.dto.MovieCreateRequestDto;
import com.example.backend.dto.MovieDetailsDto;
import com.example.backend.dto.NewActorDto;
import com.example.backend.dto.projection.CategoryProjection;
import com.example.backend.dto.projection.FilmProjection;
import com.example.backend.dto.projection.LanguageProjection;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.*;
import com.example.backend.service.film.FilmService;
import com.example.backend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock private FilmRepository filmRepository;
    @Mock private FilmActorRepository filmActorRepository;
    @Mock private FilmCategoryRepository filmCategoryRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private LanguageRepository languageRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ActorRepository actorRepository;
    @Mock private AuthUtil authUtil;
    @Mock private StoreRepository storeRepository;

    @InjectMocks
    private FilmService filmService;

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

    private Film buildFilm(Integer id, String title) {
        Language lang = new Language();
        lang.setName("English");

        Film film = new Film();
        film.setFilmId(id);
        film.setTitle(title);
        film.setDescription("desc");
        film.setReleaseYear(2024);
        film.setLanguage(lang);
        film.setRating("PG");
        film.setLength(120);
        film.setRentalDuration(3);
        film.setRentalRate(BigDecimal.valueOf(2.99));
        film.setReplacementCost(BigDecimal.valueOf(19.99));
        film.setSpecialFeatures("Trailers");
        film.setFilmActors(List.of());
        film.setFilmCategories(List.of());
        return film;
    }

    private Staff buildStaff(Integer storeId) {
        Staff s = new Staff();
        s.setStaffId(1);
        s.setStoreId(storeId);
        s.setUsername("manager");
        return s;
    }

    private MovieCreateRequestDto baseRequest() {
        MovieCreateRequestDto req = new MovieCreateRequestDto();
        req.setTitle("New Movie");
        req.setDescription("A film");
        req.setReleaseYear("2025");
        req.setLanguageId(1);
        req.setRentalDuration(3);
        req.setRentalRate(BigDecimal.valueOf(2.99));
        req.setLength(100);
        req.setReplacementCost(BigDecimal.valueOf(19.99));
        req.setRating("G");
        req.setSpecialFeatures("None");
        req.setCopies(1);
        return req;
    }

    @Test
    @DisplayName("getAllMovies — should return paged film projections")
    void shouldReturnAllMovies() {
        Page<FilmProjection> page = new PageImpl<>(List.of(filmProjection(1, "Inception")));
        when(filmRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(page);

        Page<FilmProjection> result = filmService.getAllMovies(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("getAllMovies — should return empty page when no films exist")
    void shouldReturnEmptyMoviePage() {
        when(filmRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(Page.empty());

        assertThat(filmService.getAllMovies(PageRequest.of(0, 10)).getContent()).isEmpty();
    }

    @Test
    @DisplayName("searchMovies — should return matching films by title")
    void shouldSearchMoviesByTitle() {
        Page<FilmProjection> page = new PageImpl<>(List.of(filmProjection(1, "Inception")));
        when(filmRepository.findByTitleContainingIgnoreCase(eq("incep"), any(PageRequest.class)))
                .thenReturn(page);

        Page<FilmProjection> result = filmService.searchMovies("incep", PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("searchMovies — should return empty when no title matches")
    void shouldReturnEmptyWhenNoTitleMatches() {
        when(filmRepository.findByTitleContainingIgnoreCase(eq("zzzz"), any(PageRequest.class)))
                .thenReturn(Page.empty());

        assertThat(filmService.searchMovies("zzzz", PageRequest.of(0, 10)).getContent()).isEmpty();
    }

    @Test
    @DisplayName("searchMoviesByActor — single word uses OR query on actor names")
    void shouldSearchMoviesBySingleActorName() {
        Page<FilmProjection> page = new PageImpl<>(List.of(filmProjection(1, "Cast Away")));
        when(filmRepository
                .findDistinctByFilmActors_Actor_FirstNameContainingIgnoreCaseOrFilmActors_Actor_LastNameContainingIgnoreCase(
                        eq("hanks"), eq("hanks"), any(PageRequest.class)))
                .thenReturn(page);

        Page<FilmProjection> result =
                filmService.searchMoviesByActor("hanks", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("searchMoviesByActor — two words uses AND query on first + last name")
    void shouldSearchMoviesByFullActorName() {
        Page<FilmProjection> page = new PageImpl<>(List.of(filmProjection(1, "Forrest Gump")));
        when(filmRepository
                .findDistinctByFilmActors_Actor_FirstNameContainingIgnoreCaseAndFilmActors_Actor_LastNameContainingIgnoreCase(
                        eq("Tom"), eq("Hanks"), any(PageRequest.class)))
                .thenReturn(page);

        Page<FilmProjection> result =
                filmService.searchMoviesByActor("Tom Hanks", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }


    @Test
    @DisplayName("getMoviesByCategory — should return films for given category")
    void shouldReturnMoviesByCategory() {
        Page<FilmProjection> page = new PageImpl<>(List.of(filmProjection(1, "Inception")));
        when(filmRepository.findDistinctByFilmCategories_Category_NameIgnoreCase(
                eq("Action"), any(PageRequest.class))).thenReturn(page);

        Page<FilmProjection> result =
                filmService.getMoviesByCategory("Action", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getMoviesByCategory — should return empty page for unknown category")
    void shouldReturnEmptyForUnknownCategory() {
        when(filmRepository.findDistinctByFilmCategories_Category_NameIgnoreCase(
                eq("Unknown"), any(PageRequest.class))).thenReturn(Page.empty());

        assertThat(filmService.getMoviesByCategory("Unknown", PageRequest.of(0, 10)).getContent())
                .isEmpty();
    }


    @Test
    @DisplayName("getAllLanguages — should return sorted language list")
    void shouldReturnAllLanguages() {
        LanguageProjection lp = new LanguageProjection() {
            @Override public Integer getLanguageId() { return 1; }
            @Override public String  getName()       { return "English"; }
        };
        when(languageRepository.findAllByOrderByNameAsc()).thenReturn(List.of(lp));

        List<LanguageProjection> result = filmService.getAllLanguages();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("English");
    }

    @Test
    @DisplayName("getAllCategories — should return sorted category list")
    void shouldReturnAllCategories() {
        CategoryProjection cp = new CategoryProjection() {
            @Override public Integer getCategoryId() { return 1; }
            @Override public String  getName()       { return "Action"; }
        };
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(cp));

        List<CategoryProjection> result = filmService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Action");
    }

    @Test
    @DisplayName("getMovieDetails — should return full details for existing film")
    void shouldReturnMovieDetails() {

        Actor actor = new Actor();
        actor.setActorId(1);
        actor.setFirstName("Tom");
        actor.setLastName("Hanks");

        FilmActorId faId = new FilmActorId();
        faId.setActorId(1); faId.setFilmId(1);
        FilmActor fa = new FilmActor();
        fa.setId(faId);
        fa.setActor(actor);

        Category category = new Category();
        category.setCategoryId(1);
        category.setName("Drama");

        FilmCategoryId fcId = new FilmCategoryId();
        fcId.setFilmId(1); fcId.setCategoryId(1);
        FilmCategory fc = new FilmCategory();
        fc.setId(fcId);
        fc.setCategory(category);

        Film film = buildFilm(1, "Cast Away");
        film.setFilmActors(List.of(fa));
        film.setFilmCategories(List.of(fc));

        when(filmRepository.findDetailedByFilmId(1)).thenReturn(Optional.of(film));

        MovieDetailsDto dto = filmService.getMovieDetails(1);

        assertThat(dto.getTitle()).isEqualTo("Cast Away");
        assertThat(dto.getActors()).hasSize(1);
        assertThat(dto.getActors().get(0).name()).isEqualTo("Tom Hanks");
        assertThat(dto.getCategories()).containsExactly("Drama");
        assertThat(dto.getLanguage()).isEqualTo("English");
    }

    @Test
    @DisplayName("getMovieDetails — should throw when film not found")
    void shouldThrowWhenFilmNotFoundForDetails() {
        when(filmRepository.findDetailedByFilmId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filmService.getMovieDetails(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Movie not found");
    }


    @Test
    @DisplayName("createMovie — should save film and create 1 inventory copy")
    void shouldCreateMovieSuccessfully() {
        MovieCreateRequestDto req = baseRequest();

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));

        Language lang = new Language();
        lang.setLanguageId(1);
        lang.setName("English");
        when(languageRepository.findById(1)).thenReturn(Optional.of(lang));

        Film saved = buildFilm(100, "New Movie");
        when(filmRepository.save(any(Film.class))).thenReturn(saved);

        Store store = new Store();
        store.setStoreId(1);
        when(storeRepository.findById(1)).thenReturn(Optional.of(store));

        Integer filmId = filmService.createMovie(req);

        assertThat(filmId).isEqualTo(100);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("createMovie — should create requested number of inventory copies")
    void shouldCreateRequestedCopies() {
        MovieCreateRequestDto req = baseRequest();
        req.setCopies(3);

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));

        Language lang = new Language();
        lang.setLanguageId(1);
        when(languageRepository.findById(1)).thenReturn(Optional.of(lang));

        Film saved = buildFilm(100, "New Movie");
        when(filmRepository.save(any(Film.class))).thenReturn(saved);

        Store store = new Store();
        store.setStoreId(1);
        when(storeRepository.findById(1)).thenReturn(Optional.of(store));

        filmService.createMovie(req);

        verify(inventoryRepository, times(3)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("createMovie — copies=0 defaults to 1 inventory entry")
    void shouldDefaultToOneCopyWhenZeroCopies() {
        MovieCreateRequestDto req = baseRequest();
        req.setCopies(0);

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));

        Language lang = new Language();
        lang.setLanguageId(1);
        when(languageRepository.findById(1)).thenReturn(Optional.of(lang));

        Film saved = buildFilm(100, "New Movie");
        when(filmRepository.save(any(Film.class))).thenReturn(saved);

        Store store = new Store();
        store.setStoreId(1);
        when(storeRepository.findById(1)).thenReturn(Optional.of(store));

        filmService.createMovie(req);

        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("createMovie — should link existing actors via filmActorRepository")
    void shouldLinkExistingActors() {
        MovieCreateRequestDto req = baseRequest();
        req.setActorIds(List.of(5));

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));

        Language lang = new Language();
        lang.setLanguageId(1);
        when(languageRepository.findById(1)).thenReturn(Optional.of(lang));

        Film saved = buildFilm(100, "New Movie");
        when(filmRepository.save(any(Film.class))).thenReturn(saved);

        Actor actor = new Actor();
        actor.setActorId(5);
        when(actorRepository.findById(5)).thenReturn(Optional.of(actor));

        Store store = new Store();
        store.setStoreId(1);
        when(storeRepository.findById(1)).thenReturn(Optional.of(store));

        filmService.createMovie(req);

        verify(filmActorRepository, times(1)).save(any(FilmActor.class));
    }

    @Test
    @DisplayName("createMovie — should throw when existing actor id not found")
    void shouldThrowWhenActorIdNotFound() {
        MovieCreateRequestDto req = baseRequest();
        req.setActorIds(List.of(999));

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));

        Language lang = new Language();
        lang.setLanguageId(1);
        when(languageRepository.findById(1)).thenReturn(Optional.of(lang));

        Film saved = buildFilm(100, "New Movie");
        when(filmRepository.save(any(Film.class))).thenReturn(saved);

        when(actorRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filmService.createMovie(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("createMovie — should save new actors inline and link them")
    void shouldCreateAndLinkNewActors() {
        MovieCreateRequestDto req = baseRequest();
        NewActorDto na = new NewActorDto();
        na.setFirstName("Jane");
        na.setLastName("Doe");
        req.setNewActors(List.of(na));

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));

        Language lang = new Language();
        lang.setLanguageId(1);
        when(languageRepository.findById(1)).thenReturn(Optional.of(lang));

        Film saved = buildFilm(100, "New Movie");
        when(filmRepository.save(any(Film.class))).thenReturn(saved);

        // Actor.actorId uses @GeneratedValue(IDENTITY); the service no longer
        // manually pre-computes an id, and uses saveAndFlush so Hibernate
        // populates the id before linking the FilmActor.
        Actor newActor = new Actor();
        newActor.setActorId(51);
        newActor.setFirstName("Jane");
        newActor.setLastName("Doe");
        when(actorRepository.saveAndFlush(any(Actor.class))).thenReturn(newActor);

        Store store = new Store();
        store.setStoreId(1);
        when(storeRepository.findById(1)).thenReturn(Optional.of(store));

        filmService.createMovie(req);

        ArgumentCaptor<Actor> captor = ArgumentCaptor.forClass(Actor.class);
        verify(actorRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getFirstName()).isEqualTo("Jane");
        assertThat(captor.getValue().getLastName()).isEqualTo("Doe");
        // ID is NOT manually set — Hibernate IDENTITY assigns it on flush.
        assertThat(captor.getValue().getActorId()).isNull();

        verify(filmActorRepository, times(1)).save(any(FilmActor.class));
    }

    @Test
    @DisplayName("createMovie — should link categories via filmCategoryRepository")
    void shouldLinkCategories() {
        MovieCreateRequestDto req = baseRequest();
        req.setCategoryIds(List.of(3));

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));

        Language lang = new Language();
        lang.setLanguageId(1);
        when(languageRepository.findById(1)).thenReturn(Optional.of(lang));

        Film saved = buildFilm(100, "New Movie");
        when(filmRepository.save(any(Film.class))).thenReturn(saved);

        Category cat = new Category();
        cat.setCategoryId(3);
        when(categoryRepository.findById(3)).thenReturn(Optional.of(cat));

        Store store = new Store();
        store.setStoreId(1);
        when(storeRepository.findById(1)).thenReturn(Optional.of(store));

        filmService.createMovie(req);

        verify(filmCategoryRepository, times(1)).save(any(FilmCategory.class));
    }

    @Test
    @DisplayName("createMovie — should throw when category id not found")
    void shouldThrowWhenCategoryIdNotFound() {
        MovieCreateRequestDto req = baseRequest();
        req.setCategoryIds(List.of(999));

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));

        Language lang = new Language();
        lang.setLanguageId(1);
        when(languageRepository.findById(1)).thenReturn(Optional.of(lang));

        Film saved = buildFilm(100, "New Movie");
        when(filmRepository.save(any(Film.class))).thenReturn(saved);

        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filmService.createMovie(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("createMovie — should throw when staff not found")
    void shouldThrowWhenStaffNotFound() {
        MovieCreateRequestDto req = baseRequest();

        when(authUtil.getLoggedInUsername()).thenReturn("ghost");
        when(staffRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filmService.createMovie(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Staff not found");

        verify(filmRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie — should throw when language not found")
    void shouldThrowWhenLanguageNotFound() {
        MovieCreateRequestDto req = baseRequest();

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));
        when(languageRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filmService.createMovie(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Language not found");

        verify(filmRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie — should throw when store not found")
    void shouldThrowWhenStoreNotFound() {
        MovieCreateRequestDto req = baseRequest();

        when(authUtil.getLoggedInUsername()).thenReturn("manager");
        when(staffRepository.findByUsername("manager")).thenReturn(Optional.of(buildStaff(1)));

        Language lang = new Language();
        lang.setLanguageId(1);
        when(languageRepository.findById(1)).thenReturn(Optional.of(lang));

        Film saved = buildFilm(100, "New Movie");
        when(filmRepository.save(any(Film.class))).thenReturn(saved);

        when(storeRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filmService.createMovie(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Store not found");
    }
}