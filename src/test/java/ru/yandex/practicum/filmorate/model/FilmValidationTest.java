package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldRejectBlankName() {
        Film film = createValidFilm();
        film.setName("");

        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void shouldRejectDescriptionLongerThan200Characters() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));

        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void shouldAcceptDescriptionWithExactly200Characters() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));

        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    void shouldRejectNullReleaseDate() {
        Film film = createValidFilm();
        film.setReleaseDate(null);

        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void shouldRejectZeroDuration() {
        Film film = createValidFilm();
        film.setDuration(0);

        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void shouldRejectNegativeDuration() {
        Film film = createValidFilm();
        film.setDuration(-1);

        assertFalse(validator.validate(film).isEmpty());
    }

    private Film createValidFilm() {
        Film film = new Film();

        film.setName("lanterns");
        film.setDescription("Sci-Fi");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);

        return film;
    }
}