package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();

        GenreStorage genreStorage = new GenreStorage() {
            @Override
            public List<Genre> getAll() {
                return List.of();
            }

            @Override
            public Genre getById(int id) {
                Genre genre = new Genre();
                genre.setId(id);

                switch (id) {
                    case 1 -> genre.setName("Комедия");
                    case 2 -> genre.setName("Драма");
                    case 3 -> genre.setName("Мультфильм");
                    case 4 -> genre.setName("Триллер");
                    case 5 -> genre.setName("Документальный");
                    case 6 -> genre.setName("Боевик");
                    default -> throw new NotFoundException(
                            "Жанр с id=" + id + " не найден"
                    );
                }

                return genre;
            }
        };

        MpaStorage mpaStorage = new MpaStorage() {
            @Override
            public List<Mpa> getAll() {
                return List.of(
                        createMpa(1, "G"),
                        createMpa(2, "PG"),
                        createMpa(3, "PG-13"),
                        createMpa(4, "R"),
                        createMpa(5, "NC-17")
                );
            }

            @Override
            public Mpa getById(int id) {
                return getAll().stream()
                        .filter(mpa -> mpa.getId() == id)
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(
                                "Рейтинг MPA с id=" + id + " не найден"
                        ));
            }
        };

        FilmService filmService = new FilmService(
                filmStorage,
                userStorage,
                new GenreService(genreStorage),
                new MpaService(mpaStorage)
        );

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
        Film created = controller.createFilm(film);

        Film updatedFilm = createFilm();
        updatedFilm.setId(created.getId());
        updatedFilm.setName("DogGod");

        Film updated = controller.updateFilm(updatedFilm);

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

    private Mpa createMpa(int id, String name) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        mpa.setName(name);
        return mpa;
    }
}