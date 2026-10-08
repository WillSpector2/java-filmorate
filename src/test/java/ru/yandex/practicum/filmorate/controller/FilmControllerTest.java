package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);

        controller = new FilmController(filmService);
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
                NotFoundException.class,
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