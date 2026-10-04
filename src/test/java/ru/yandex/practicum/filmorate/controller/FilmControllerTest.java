package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    @Test
    void shouldCreateFilm() {
        Film film = createFilm();

        Film created = controller.createFilm(film);

        assertEquals(1, created.getId());
        assertEquals(1, controller.getFilms().size());
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film film = createFilm();
        controller.createFilm(film);

        film.setName("DogGod");

        Film updated = controller.updateFilm(film);

        assertEquals("DogGod", updated.getName());
        assertEquals(1, controller.getFilms().size());
    }

    @Test
    void shouldRejectFilmReleasedBeforeFirstFilm() {
        Film film = createFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(
                ValidationException.class,
                () -> controller.createFilm(film)
        );
    }

    @Test
    void shouldAcceptFilmReleasedOnFirstFilmDate() {
        Film film = createFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        Film created = controller.createFilm(film);

        assertEquals(1, created.getId());
    }

    @Test
    void shouldRejectUpdateOfUnknownFilm() {
        Film film = createFilm();
        film.setId(999);

        assertThrows(
                ValidationException.class,
                () -> controller.updateFilm(film)
        );
    }

    private Film createFilm() {
        Film film = new Film();

        film.setName("Inception");
        film.setDescription("Science fiction");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);

        return film;
    }
}