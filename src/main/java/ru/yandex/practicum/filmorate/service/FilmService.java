package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaService mpaService;

    @Autowired
    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            GenreService genreService,
            MpaService mpaService
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    public Film createFilm(Film film) {
        normalizeFilm(film);
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        Film existingFilm = filmStorage.getById(film.getId());

        // Обычное обновление фильма не должно удалять существующие лайки.
        film.setLikes(existingFilm.getLikes());

        normalizeFilm(film);
        return filmStorage.update(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getAll();
    }

    public Film getFilmById(int id) {
        return filmStorage.getById(id);
    }

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        film.getLikes().add(userId);

        filmStorage.update(film);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        film.getLikes().remove(userId);

        filmStorage.update(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator
                        .comparingInt((Film film) -> film.getLikes().size())
                        .reversed())
                .limit(count)
                .toList();
    }

    private void normalizeFilm(Film film) {
        if (film.getMpa() != null) {
            Mpa mpa = mpaService.getMpaById(film.getMpa().getId());
            film.setMpa(mpa);
        }

        if (film.getGenres() != null) {
            List<Genre> genres = new ArrayList<>();
            Set<Integer> genreIds = new HashSet<>();

            for (Genre genre : film.getGenres()) {
                Genre actualGenre =
                        genreService.getGenreById(genre.getId());

                if (genreIds.add(actualGenre.getId())) {
                    genres.add(actualGenre);
                }
            }

            film.setGenres(List.copyOf(genres));
        }
    }
}