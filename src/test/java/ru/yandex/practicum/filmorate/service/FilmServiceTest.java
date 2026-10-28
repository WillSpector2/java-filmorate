package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmServiceTest {

    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FilmService filmService;

    private User firstUser;
    private User secondUser;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();

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

        filmService = new FilmService(
                filmStorage,
                userStorage,
                new GenreService(genreStorage),
                new MpaService(mpaStorage)
        );

        firstUser = createUser("first@example.com", "first");
        secondUser = createUser("second@example.com", "second");

        userStorage.add(firstUser);
        userStorage.add(secondUser);
    }

    @Test
    void shouldAddLike() {
        Film film = createFilm("Inception");
        filmStorage.add(film);

        filmService.addLike(film.getId(), firstUser.getId());

        assertEquals(1, film.getLikes().size());
        assertTrue(film.getLikes().contains(firstUser.getId()));
    }

    @Test
    void shouldNotAddDuplicateLike() {
        Film film = createFilm("Inception");
        filmStorage.add(film);

        filmService.addLike(film.getId(), firstUser.getId());
        filmService.addLike(film.getId(), firstUser.getId());

        assertEquals(1, film.getLikes().size());
    }

    @Test
    void shouldRemoveLike() {
        Film film = createFilm("Inception");
        filmStorage.add(film);

        filmService.addLike(film.getId(), firstUser.getId());
        filmService.removeLike(film.getId(), firstUser.getId());

        assertEquals(0, film.getLikes().size());
    }

    @Test
    void shouldReturnPopularFilmsInDescendingOrder() {
        Film firstFilm = createFilm("Inception");
        Film secondFilm = createFilm("The Dark Knight");
        Film thirdFilm = createFilm("Interstellar");

        filmStorage.add(firstFilm);
        filmStorage.add(secondFilm);
        filmStorage.add(thirdFilm);

        filmService.addLike(firstFilm.getId(), firstUser.getId());

        filmService.addLike(secondFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), secondUser.getId());

        filmService.addLike(thirdFilm.getId(), firstUser.getId());
        filmService.addLike(thirdFilm.getId(), secondUser.getId());

        List<Film> popularFilms = filmService.getPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(secondFilm.getId(), popularFilms.get(0).getId());
        assertEquals(thirdFilm.getId(), popularFilms.get(1).getId());
    }

    @Test
    void shouldLimitNumberOfPopularFilms() {
        Film firstFilm = createFilm("Inception");
        Film secondFilm = createFilm("The Dark Knight");

        filmStorage.add(firstFilm);
        filmStorage.add(secondFilm);

        filmService.addLike(firstFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), firstUser.getId());

        List<Film> popularFilms = filmService.getPopularFilms(1);

        assertEquals(1, popularFilms.size());
    }

    private Film createFilm(String name) {
        Film film = new Film();

        film.setName(name);
        film.setDescription("Science fiction");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(120);

        return film;
    }

    private User createUser(String email, String login) {
        User user = new User();

        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        return user;
    }

    private Mpa createMpa(int id, String name) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        mpa.setName(name);
        return mpa;
    }
}