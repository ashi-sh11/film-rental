package com.example.backend.repository;

import com.example.backend.entity.Category;
import com.example.backend.entity.Film;
import com.example.backend.entity.FilmCategory;
import com.example.backend.entity.FilmCategoryId;
import com.example.backend.entity.Language;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FilmCategoryRepositoryTest {

    @Autowired
    private FilmCategoryRepository filmCategoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Pageable pageable;

    @BeforeAll
    void resetTinyIntAutoIncrement() {
        Integer nextCat = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(category_id),0)+1 FROM category", Integer.class);
        Integer nextLang = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(language_id),0)+1 FROM language", Integer.class);
        jdbcTemplate.execute("ALTER TABLE category AUTO_INCREMENT = " + nextCat);
        jdbcTemplate.execute("ALTER TABLE language AUTO_INCREMENT = " + nextLang);
    }

    @BeforeEach
    void setUp() {

        pageable = PageRequest.of(0, 10);

        Language language = new Language();
        language.setName("T");
        language.setLastUpdate(LocalDateTime.now());
        entityManager.persist(language);

        Category actionCategory = new Category();
        actionCategory.setName("TestActionCategory");
        actionCategory.setLastUpdate(LocalDateTime.now());

        Category comedyCategory = new Category();
        comedyCategory.setName("TestComedyCategory");
        comedyCategory.setLastUpdate(LocalDateTime.now());

        entityManager.persist(actionCategory);
        entityManager.persist(comedyCategory);

        Film film1 = new Film();
        film1.setTitle("Batman Begins");
        film1.setDescription("Action movie");
        film1.setReleaseYear(2005);
        film1.setRentalDuration(5);
        film1.setRentalRate(BigDecimal.valueOf(4.99));
        film1.setLength(120);
        film1.setReplacementCost(BigDecimal.valueOf(19.99));
        film1.setRating("PG-13");
        film1.setSpecialFeatures("Trailers");
        film1.setLastUpdate(LocalDateTime.now());
        film1.setLanguage(language);

        Film film2 = new Film();
        film2.setTitle("The Mask");
        film2.setDescription("Comedy movie");
        film2.setReleaseYear(1994);
        film2.setRentalDuration(5);
        film2.setRentalRate(BigDecimal.valueOf(3.99));
        film2.setLength(110);
        film2.setReplacementCost(BigDecimal.valueOf(17.99));
        film2.setRating("PG");
        film2.setSpecialFeatures("Deleted Scenes");
        film2.setLastUpdate(LocalDateTime.now());
        film2.setLanguage(language);

        entityManager.persist(film1);
        entityManager.persist(film2);
        entityManager.flush();

        FilmCategory filmCategory1 = new FilmCategory();
        filmCategory1.setId(new FilmCategoryId());
        filmCategory1.setFilm(film1);
        filmCategory1.setCategory(actionCategory);
        filmCategory1.setLastUpdate(LocalDateTime.now());

        FilmCategory filmCategory2 = new FilmCategory();
        filmCategory2.setId(new FilmCategoryId());
        filmCategory2.setFilm(film2);
        filmCategory2.setCategory(comedyCategory);
        filmCategory2.setLastUpdate(LocalDateTime.now());

        filmCategoryRepository.save(filmCategory1);
        filmCategoryRepository.save(filmCategory2);
    }

    @Test
    @DisplayName("Should find film categories by category name")
    void testFindByCategoryNameIgnoreCase() {

        Page<FilmCategory> result =
                filmCategoryRepository.findByCategory_NameIgnoreCase(
                        "TestActionCategory",
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);

        FilmCategory filmCategory = result.getContent().get(0);

        assertThat(filmCategory.getCategory().getName())
                .isEqualTo("TestActionCategory");

        assertThat(filmCategory.getFilm().getTitle())
                .isEqualTo("Batman Begins");
    }
}