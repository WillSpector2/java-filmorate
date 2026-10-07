package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate FIRST_FILM_DATE =
            LocalDate.of(1895, 12, 28);

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        validateFilm(film);

        Film createdFilm = filmService.createFilm(film);

        log.info("Добавлен фильм: {}", createdFilm);

        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        validateFilm(film);

        Film updatedFilm = filmService.updateFilm(film);

        log.info(
                "Обновлён фильм с id={}: {}",
                updatedFilm.getId(),
                updatedFilm
        );

        return updatedFilm;
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable int id,
            @PathVariable int userId) {

        filmService.addLike(id, userId);

        log.info(
                "Пользователь с id={} поставил лайк фильму с id={}",
                userId,
                id
        );
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(
            @PathVariable int id,
            @PathVariable int userId) {

        filmService.removeLike(id, userId);

        log.info(
                "Пользователь с id={} удалил лайк у фильма с id={}",
                userId,
                id
        );
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count) {

        return filmService.getPopularFilms(count);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {

            log.error(
                    "Дата релиза фильма {} раньше 28 декабря 1895 года",
                    film.getName()
            );

            throw new ValidationException(
                    "Дата релиза не может быть раньше 28 декабря 1895 года"
            );
        }

        if (film.getDuration() <= 0) {
            log.error(
                    "Некорректная продолжительность фильма {}: {}",
                    film.getName(),
                    film.getDuration()
            );

            throw new ValidationException(
                    "Продолжительность фильма должна быть положительным числом"
            );
        }
    }
}