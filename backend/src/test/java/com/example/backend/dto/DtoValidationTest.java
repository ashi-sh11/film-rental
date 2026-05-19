package com.example.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Programmatic Bean Validation tests for the request DTOs. Runs the validator
 * directly (no Spring context) so each rule is exercised in isolation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DtoValidationTest {

    private ValidatorFactory factory;
    private Validator validator;

    @BeforeAll
    void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    void tearDown() {
        factory.close();
    }

    // ---------- Helpers ----------

    private CustomerRequestDto validCustomer() {
        CustomerRequestDto r = new CustomerRequestDto();
        r.setFirstName("Alice");
        r.setLastName("Smith");
        r.setEmail("alice@example.com");
        r.setStoreId(1);
        r.setAddress("123 Main St");
        r.setDistrict("Some District");
        r.setCityId(1);
        r.setPostalCode("90210");
        r.setPhone("5551230000");
        return r;
    }

    private StaffRegisterDto validStaff() {
        StaffRegisterDto r = new StaffRegisterDto();
        r.setFirstName("Alice");
        r.setLastName("Smith");
        r.setUsername("alice_d");
        r.setEmail("alice@example.com");
        r.setPassword("Test@1234");
        r.setStoreId(1);
        r.setAddress("123 Main St");
        r.setDistrict("D1");
        r.setCityId(1);
        r.setPostalCode("90210");
        r.setPhone("5551230000");
        return r;
    }

    private MovieCreateRequestDto validMovie() {
        MovieCreateRequestDto m = new MovieCreateRequestDto();
        m.setTitle("New Film");
        m.setLanguageId(1);
        m.setRentalRate(new BigDecimal("2.99"));
        m.setReplacementCost(new BigDecimal("19.99"));
        m.setReleaseYear(String.valueOf(Year.now().getValue()));
        m.setLength(100);
        m.setRating("G");
        m.setRentalDuration(5);
        m.setCopies(1);
        return m;
    }

    private NewActorDto validActor() {
        NewActorDto a = new NewActorDto();
        a.setFirstName("Tom");
        a.setLastName("Hanks");
        return a;
    }

    private boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String field) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    // ---------- CustomerRequestDto ----------

    @Test
    @DisplayName("Customer — fully valid request has no violations")
    void customerValidPasses() {
        assertThat(validator.validate(validCustomer())).isEmpty();
    }

    @Test
    @DisplayName("Customer — phone must be exactly 10 digits")
    void customerPhoneMustBe10Digits() {
        CustomerRequestDto r = validCustomer();

        r.setPhone("123");
        assertThat(hasViolationOn(validator.validate(r), "phone")).isTrue();

        r.setPhone("12345678901");
        assertThat(hasViolationOn(validator.validate(r), "phone")).isTrue();

        r.setPhone("555-123-0000");
        assertThat(hasViolationOn(validator.validate(r), "phone")).isTrue();

        r.setPhone("555 123 0000");
        assertThat(hasViolationOn(validator.validate(r), "phone")).isTrue();

        r.setPhone("abcdefghij");
        assertThat(hasViolationOn(validator.validate(r), "phone")).isTrue();

        r.setPhone("5551230000");
        assertThat(hasViolationOn(validator.validate(r), "phone")).isFalse();
    }

    @Test
    @DisplayName("Customer — phone is required")
    void customerPhoneRequired() {
        CustomerRequestDto r = validCustomer();
        r.setPhone(null);
        assertThat(hasViolationOn(validator.validate(r), "phone")).isTrue();
    }

    @Test
    @DisplayName("Customer — firstName must start with a letter and only use letters/spaces/'/-")
    void customerFirstNamePattern() {
        CustomerRequestDto r = validCustomer();

        r.setFirstName("Mary-Jane");
        assertThat(hasViolationOn(validator.validate(r), "firstName")).isFalse();

        r.setFirstName("O'Brien");
        assertThat(hasViolationOn(validator.validate(r), "firstName")).isFalse();

        r.setFirstName("Alice123");
        assertThat(hasViolationOn(validator.validate(r), "firstName")).isTrue();

        r.setFirstName("123Alice");
        assertThat(hasViolationOn(validator.validate(r), "firstName")).isTrue();

        r.setFirstName("Alice@");
        assertThat(hasViolationOn(validator.validate(r), "firstName")).isTrue();
    }

    @Test
    @DisplayName("Customer — postal code allows digits/letters/space/hyphen, 3-10 chars; may be empty")
    void customerPostalPattern() {
        CustomerRequestDto r = validCustomer();

        r.setPostalCode("");
        assertThat(hasViolationOn(validator.validate(r), "postalCode")).isFalse();

        r.setPostalCode("12345");
        assertThat(hasViolationOn(validator.validate(r), "postalCode")).isFalse();

        r.setPostalCode("SW1A 1AA");
        assertThat(hasViolationOn(validator.validate(r), "postalCode")).isFalse();

        r.setPostalCode("12");
        assertThat(hasViolationOn(validator.validate(r), "postalCode")).isTrue();

        r.setPostalCode("12345678901");
        assertThat(hasViolationOn(validator.validate(r), "postalCode")).isTrue();

        r.setPostalCode("12_34");
        assertThat(hasViolationOn(validator.validate(r), "postalCode")).isTrue();
    }

    @Test
    @DisplayName("Customer — email must be valid format")
    void customerEmailFormat() {
        CustomerRequestDto r = validCustomer();
        r.setEmail("not-an-email");
        assertThat(hasViolationOn(validator.validate(r), "email")).isTrue();
    }

    @Test
    @DisplayName("Customer — storeId/cityId must be positive")
    void customerIdsPositive() {
        CustomerRequestDto r = validCustomer();
        r.setStoreId(0);
        r.setCityId(-1);
        Set<ConstraintViolation<CustomerRequestDto>> v = validator.validate(r);
        assertThat(hasViolationOn(v, "storeId")).isTrue();
        assertThat(hasViolationOn(v, "cityId")).isTrue();
    }

    // ---------- StaffRegisterDto ----------

    @Test
    @DisplayName("Staff — fully valid request has no violations")
    void staffValidPasses() {
        assertThat(validator.validate(validStaff())).isEmpty();
    }

    @Test
    @DisplayName("Staff — phone must be exactly 10 digits")
    void staffPhoneMustBe10Digits() {
        StaffRegisterDto r = validStaff();
        r.setPhone("12345");
        assertThat(hasViolationOn(validator.validate(r), "phone")).isTrue();
        r.setPhone("5551230000");
        assertThat(hasViolationOn(validator.validate(r), "phone")).isFalse();
    }

    @Test
    @DisplayName("Staff — username must be 3-16 chars of [A-Za-z0-9_]")
    void staffUsernamePattern() {
        StaffRegisterDto r = validStaff();

        r.setUsername("ab");
        assertThat(hasViolationOn(validator.validate(r), "username")).isTrue();

        r.setUsername("has spaces");
        assertThat(hasViolationOn(validator.validate(r), "username")).isTrue();

        r.setUsername("ok_user_99");
        assertThat(hasViolationOn(validator.validate(r), "username")).isFalse();
    }

    @Test
    @DisplayName("Staff — password is required and must satisfy complexity rules")
    void staffPasswordComplexity() {
        StaffRegisterDto r = validStaff();

        // Required
        r.setPassword(null);
        assertThat(hasViolationOn(validator.validate(r), "password")).isTrue();

        // Too short
        r.setPassword("Aa1!");
        assertThat(hasViolationOn(validator.validate(r), "password")).isTrue();

        // Missing uppercase
        r.setPassword("test@1234");
        assertThat(hasViolationOn(validator.validate(r), "password")).isTrue();

        // Missing lowercase
        r.setPassword("TEST@1234");
        assertThat(hasViolationOn(validator.validate(r), "password")).isTrue();

        // Missing digit
        r.setPassword("Test@abcd");
        assertThat(hasViolationOn(validator.validate(r), "password")).isTrue();

        // Missing special character
        r.setPassword("Test1234ab");
        assertThat(hasViolationOn(validator.validate(r), "password")).isTrue();

        // All four classes present, 9 chars — passes
        r.setPassword("Test@1234");
        assertThat(hasViolationOn(validator.validate(r), "password")).isFalse();

        // 16 chars — at the upper bound, passes
        r.setPassword("Test@123456789Aa");
        assertThat(hasViolationOn(validator.validate(r), "password")).isFalse();

        // 17 chars — over the upper bound, fails
        r.setPassword("Test@1234567890Aa");
        assertThat(hasViolationOn(validator.validate(r), "password")).isTrue();
    }

    // ---------- MovieCreateRequestDto ----------

    @Test
    @DisplayName("Movie — fully valid request has no violations")
    void movieValidPasses() {
        assertThat(validator.validate(validMovie())).isEmpty();
    }

    @Test
    @DisplayName("Movie — release year cannot exceed current year")
    void movieReleaseYearCeiling() {
        MovieCreateRequestDto m = validMovie();
        int future = Year.now().getValue() + 1;
        m.setReleaseYear(String.valueOf(future));
        assertThat(hasViolationOn(validator.validate(m), "releaseYearWithinRange")).isTrue();
    }

    @Test
    @DisplayName("Movie — release year cannot be before 1888 (first known film)")
    void movieReleaseYearFloor() {
        MovieCreateRequestDto m = validMovie();
        m.setReleaseYear("1700");
        assertThat(hasViolationOn(validator.validate(m), "releaseYearWithinRange")).isTrue();
    }

    @Test
    @DisplayName("Movie — release year of current year passes")
    void movieReleaseYearCurrentPasses() {
        MovieCreateRequestDto m = validMovie();
        m.setReleaseYear(String.valueOf(Year.now().getValue()));
        assertThat(hasViolationOn(validator.validate(m), "releaseYearWithinRange")).isFalse();
    }

    @Test
    @DisplayName("Movie — rating must be one of G/PG/PG-13/R/NC-17")
    void movieRatingEnum() {
        MovieCreateRequestDto m = validMovie();
        m.setRating("XYZ");
        assertThat(hasViolationOn(validator.validate(m), "rating")).isTrue();
        m.setRating("NC-17");
        assertThat(hasViolationOn(validator.validate(m), "rating")).isFalse();
    }

    @Test
    @DisplayName("Movie — rental duration must be 1-30 days")
    void movieRentalDurationRange() {
        MovieCreateRequestDto m = validMovie();
        m.setRentalDuration(0);
        assertThat(hasViolationOn(validator.validate(m), "rentalDuration")).isTrue();
        m.setRentalDuration(31);
        assertThat(hasViolationOn(validator.validate(m), "rentalDuration")).isTrue();
        m.setRentalDuration(7);
        assertThat(hasViolationOn(validator.validate(m), "rentalDuration")).isFalse();
    }

    @Test
    @DisplayName("Movie — rental rate cannot be negative and must have at most 2 decimals")
    void movieRentalRateConstraints() {
        MovieCreateRequestDto m = validMovie();
        m.setRentalRate(new BigDecimal("-0.01"));
        assertThat(hasViolationOn(validator.validate(m), "rentalRate")).isTrue();
        m.setRentalRate(new BigDecimal("4.99"));
        assertThat(hasViolationOn(validator.validate(m), "rentalRate")).isFalse();
    }

    @Test
    @DisplayName("Movie — copies must be 1-100")
    void movieCopiesRange() {
        MovieCreateRequestDto m = validMovie();
        m.setCopies(0);
        assertThat(hasViolationOn(validator.validate(m), "copies")).isTrue();
        m.setCopies(101);
        assertThat(hasViolationOn(validator.validate(m), "copies")).isTrue();
        m.setCopies(5);
        assertThat(hasViolationOn(validator.validate(m), "copies")).isFalse();
    }

    // ---------- NewActorDto ----------

    @Test
    @DisplayName("Actor — names must be non-blank and letters/spaces/'/-")
    void actorNamesPattern() {
        NewActorDto a = validActor();
        assertThat(validator.validate(a)).isEmpty();

        a.setFirstName("");
        assertThat(hasViolationOn(validator.validate(a), "firstName")).isTrue();

        a.setFirstName("123");
        assertThat(hasViolationOn(validator.validate(a), "firstName")).isTrue();

        a.setFirstName("Mary-Jane");
        assertThat(hasViolationOn(validator.validate(a), "firstName")).isFalse();
    }

    // ---------- RentalRequestDto ----------

    @Test
    @DisplayName("Rental — all three ids are required and must be positive")
    void rentalIdsRequiredPositive() {
        RentalRequestDto r = new RentalRequestDto();
        Set<ConstraintViolation<RentalRequestDto>> v = validator.validate(r);
        assertThat(hasViolationOn(v, "inventoryId")).isTrue();
        assertThat(hasViolationOn(v, "customerId")).isTrue();
        assertThat(hasViolationOn(v, "staffId")).isTrue();

        r.setInventoryId(-1);
        r.setCustomerId(0);
        r.setStaffId(0);
        v = validator.validate(r);
        assertThat(hasViolationOn(v, "inventoryId")).isTrue();
        assertThat(hasViolationOn(v, "customerId")).isTrue();
        assertThat(hasViolationOn(v, "staffId")).isTrue();

        r.setInventoryId(1);
        r.setCustomerId(1);
        r.setStaffId(1);
        assertThat(validator.validate(r)).isEmpty();
    }

    // ---------- LoginRequestDto ----------

    @Test
    @DisplayName("Login — username and password required")
    void loginRequiresBoth() {
        LoginRequestDto r = new LoginRequestDto();
        Set<ConstraintViolation<LoginRequestDto>> v = validator.validate(r);
        assertThat(hasViolationOn(v, "username")).isTrue();
        assertThat(hasViolationOn(v, "password")).isTrue();

        r.setUsername("u");
        r.setPassword("p");
        assertThat(validator.validate(r)).isEmpty();
    }
}
