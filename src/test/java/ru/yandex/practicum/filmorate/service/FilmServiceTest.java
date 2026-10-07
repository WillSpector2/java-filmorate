package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
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
        filmService = new FilmService(filmStorage, userStorage);

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
}