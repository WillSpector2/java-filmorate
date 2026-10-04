package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate FIRST_FILM_DATE =
            LocalDate.of(1895, 12, 28);

    private final List<Film> films = new ArrayList<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        validateFilm(film);

        film.setId(getNextId());

        films.add(film);

        log.info("Добавлен фильм: {}", film);

        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        validateFilm(film);

        for (int i = 0; i < films.size(); i++) {
            Film oldFilm = films.get(i);

            if (oldFilm.getId() == film.getId()) {
                films.set(i, film);

                log.info("Обновлён фильм с id={}: {}", film.getId(), film);

                return film;
            }
        }

        log.error("Фильм с id={} не найден", film.getId());

        throw new ValidationException(
                "Фильм с id=" + film.getId() + " не найден"
        );
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films);
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

    private int getNextId() {
        return films.stream()
                .mapToInt(Film::getId)
                .max()
                .orElse(0) + 1;
    }
}
